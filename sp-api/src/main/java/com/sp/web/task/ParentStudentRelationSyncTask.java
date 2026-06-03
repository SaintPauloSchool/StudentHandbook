package com.sp.web.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sp.common.utils.WeChatWorkSchoolUtils;
import com.sp.common.utils.http.HttpUtils;
import com.sp.system.entity.DepartmentParentBinding;
import com.sp.system.service.DepartmentParentBindingService;
import com.sp.system.service.DepartmentService;
import com.sp.system.service.IParentStudentRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 家長學生關係同步定時任務
 *
 */
@Component
public class ParentStudentRelationSyncTask {

    private static final Logger logger = LoggerFactory.getLogger(ParentStudentRelationSyncTask.class);

    @Autowired
    private WeChatWorkSchoolUtils weChatWorkSchoolUtils;

    @Autowired
    private IParentStudentRelationService parentStudentRelationService;

    @Autowired
    private DepartmentParentBindingService departmentParentBindingService;

    @Autowired
    private DepartmentService departmentService;

    private static final AtomicBoolean isExecuting = new AtomicBoolean(false);

    /**
     * 每天凌晨0點30分執行部門家長數據同步
     *
     */
    //@Scheduled(cron = "0 30 0 * * ?")
    public void syncParentStudentRelationData() {
        // 使用AtomicBoolean確保同一時間只有一個實例在執行
        if (!isExecuting.compareAndSet(false, true)) {
            logger.info("家長學生關係同步任務已在執行中，跳過本次執行");
            return;
        }

        try {
            // 動態獲取目標部門 ID 列表
            List<Long> targetDepartmentIds = departmentService.getClassDepartmentId();
            // 確保部門 ID 列表不爲空
            if (targetDepartmentIds == null || targetDepartmentIds.isEmpty()) {
                logger.error("無法獲取目標部門 ID，同步任務終止");
               return;
            }
            // 打印目標部門 ID
            logger.info("成功獲取到 {} 個目標部門 ID: {}", targetDepartmentIds.size(), targetDepartmentIds);
            // 遍歷每個部門 ID 進行同步
            for (Long targetDepartmentId : targetDepartmentIds) {
                logger.info("開始執行部門 ID {} 的家長學生關係同步", targetDepartmentId);
                
                // 獲取 access_token 並調用獲取部門家長列表接口
                String accessToken = weChatWorkSchoolUtils.getAccessToken();
                String parentListUrl = "https://qyapi.weixin.qq.com/cgi-bin/school/user/list_parent?access_token=" + accessToken + "&department_id=" + targetDepartmentId;

                String parentResponse = HttpUtils.sendGet(parentListUrl);
                logger.info("獲取部門家長列表結果：{}", parentResponse);

                // 解析家長列表並保存到數據庫
                JSONObject parentJson = JSONObject.parseObject(parentResponse);
                // 不存在錯誤編碼則處裡數據
                if (parentJson.getInteger("errcode") != null && parentJson.getInteger("errcode") == 0) {
                    // 獲取家長數據
                    JSONArray parentsArray = parentJson.getJSONArray("parents");
                    // 家長數據存在，則處裡家長數據
                    if (parentsArray != null && !parentsArray.isEmpty()) {

                        // 記錄獲取到的家長數據
                        logger.info("部門 ID {} 成功獲取到 {} 個家長信息", targetDepartmentId, parentsArray.size());

                        // 獲取當前部門下所有已存在的家長綁定記錄
                        List<DepartmentParentBinding> existingBindings = departmentParentBindingService.selectByDepartmentId(targetDepartmentId);

                        // 創建現有記錄的映射，便於快速查找
                        Map<String, DepartmentParentBinding> existingBindingMap = new HashMap<>();
                        for (DepartmentParentBinding binding : existingBindings) {
                            existingBindingMap.put(binding.getParentUserId(), binding);
                        }

                        // 創建用於保存的 家長 - 孩子綁定記錄
                        Set<String> currentParentUserIds = new HashSet<>();

                        // 遍歷家長數據並保存到數據庫
                        for (int i = 0; i < parentsArray.size(); i++) {
                            // 獲取當前家長數據
                            JSONObject parentObj = parentsArray.getJSONObject(i);

                            String parentUserId = parentObj.getString("parent_userid");
                            String mobile = parentObj.getString("mobile");
                            String externalUserid = parentObj.getString("external_userid");

                            // 記錄當前處理的家長 ID
                            currentParentUserIds.add(parentUserId);

                            // 處理孩子信息數組
                            JSONArray childrenArray = parentObj.getJSONArray("children");

                            // 委託給 Service 層處理家長學生關係同步
                            departmentParentBindingService.processParentChildren(targetDepartmentId, parentUserId, childrenArray, existingBindingMap);

                            // 創建並保存家長學生關係記錄（如果有孩子的話）
                            if (childrenArray != null && !childrenArray.isEmpty()) {
                                // 遍歷孩子信息
                                for (int j = 0; j < childrenArray.size(); j++) {
                                    JSONObject childObj = childrenArray.getJSONObject(j);
                                    String childStudentUserId = childObj.getString("student_userid");
                                    String relation = childObj.getString("relation");
                                    String name = childObj.getString("name");
                                    // 委託給 Service 層創建家長 - 孩子關係記錄
                                    parentStudentRelationService.createAndSaveParentStudentRelation(parentUserId, childStudentUserId, name, relation, mobile, externalUserid);
                                }
                            }
                        }
                        // 委託給 Service 層刪除不再存在的家長綁定記錄
                        departmentParentBindingService.deleteObsoleteParentBindings(existingBindings, currentParentUserIds, targetDepartmentId);
                    } else {
                        logger.info("部門 ID {} 的家長列表爲空", targetDepartmentId);
                    }
                } else {
                    logger.error("獲取部門 ID {} 的家長列表失敗：{}", targetDepartmentId, parentJson.getString("errmsg"));
                }
            }
        } catch (Exception e) {
            logger.error("同步家長學生關係數據失敗", e);
        } finally {
            // 確保執行完成後釋放鎖
            isExecuting.set(false);
        }
        logger.info("家長學生關係同步任務執行完成");
    }
}