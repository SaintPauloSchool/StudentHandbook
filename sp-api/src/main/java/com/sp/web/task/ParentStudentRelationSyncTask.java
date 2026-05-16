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
 * 家长学生关系同步定时任务
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
     * 每天凌晨0点30分执行部门家长数据同步
     *
     */
    @Scheduled(cron = "0 30 0 * * ?")
    public void syncParentStudentRelationData() {
        // 使用AtomicBoolean确保同一时间只有一个实例在执行
        if (!isExecuting.compareAndSet(false, true)) {
            logger.info("家长学生关系同步任务已在执行中，跳过本次执行");
            return;
        }

        try {
            // 动态获取目标部门 ID 列表
            List<Long> targetDepartmentIds = departmentService.getClassDepartmentId();
            // 确保部门 ID 列表不为空
            if (targetDepartmentIds == null || targetDepartmentIds.isEmpty()) {
                logger.error("无法获取目标部门 ID，同步任务终止");
               return;
            }
            // 打印目标部门 ID
            logger.info("成功获取到 {} 个目标部门 ID: {}", targetDepartmentIds.size(), targetDepartmentIds);
            // 遍历每个部门 ID 进行同步
            for (Long targetDepartmentId : targetDepartmentIds) {
                logger.info("开始执行部门 ID {} 的家长学生关系同步", targetDepartmentId);
                
                // 获取 access_token 并调用获取部门家长列表接口
                String accessToken = weChatWorkSchoolUtils.getAccessToken();
                String parentListUrl = "https://qyapi.weixin.qq.com/cgi-bin/school/user/list_parent?access_token=" + accessToken + "&department_id=" + targetDepartmentId;

                String parentResponse = HttpUtils.sendGet(parentListUrl);
                logger.info("获取部门家长列表结果：{}", parentResponse);

                // 解析家长列表并保存到数据库
                JSONObject parentJson = JSONObject.parseObject(parentResponse);
                // 不存在錯誤編碼則處裡數據
                if (parentJson.getInteger("errcode") != null && parentJson.getInteger("errcode") == 0) {
                    // 獲取家长数据
                    JSONArray parentsArray = parentJson.getJSONArray("parents");
                    // 家長數據存在，則處裡家长数据
                    if (parentsArray != null && !parentsArray.isEmpty()) {

                        // 記錄獲取到的家長數據
                        logger.info("部门 ID {} 成功获取到 {} 个家长信息", targetDepartmentId, parentsArray.size());

                        // 获取当前部门下所有已存在的家长绑定记录
                        List<DepartmentParentBinding> existingBindings = departmentParentBindingService.selectByDepartmentId(targetDepartmentId);

                        // 创建现有记录的映射，便于快速查找
                        Map<String, DepartmentParentBinding> existingBindingMap = new HashMap<>();
                        for (DepartmentParentBinding binding : existingBindings) {
                            existingBindingMap.put(binding.getParentUserId(), binding);
                        }

                        // 创建用于保存的 家长 - 孩子绑定记录
                        Set<String> currentParentUserIds = new HashSet<>();

                        // 遍历家长数据并保存到数据库
                        for (int i = 0; i < parentsArray.size(); i++) {
                            // 获取當前家长数据
                            JSONObject parentObj = parentsArray.getJSONObject(i);

                            String parentUserId = parentObj.getString("parent_userid");
                            String mobile = parentObj.getString("mobile");
                            String externalUserid = parentObj.getString("external_userid");

                            // 记录当前处理的家长 ID
                            currentParentUserIds.add(parentUserId);

                            // 处理孩子信息数组
                            JSONArray childrenArray = parentObj.getJSONArray("children");

                            // 委托给 Service 层处理家长学生关系同步
                            departmentParentBindingService.processParentChildren(targetDepartmentId, parentUserId, childrenArray, existingBindingMap);

                            // 创建并保存家长学生关系记录（如果有孩子的话）
                            if (childrenArray != null && !childrenArray.isEmpty()) {
                                // 遍历孩子信息
                                for (int j = 0; j < childrenArray.size(); j++) {
                                    JSONObject childObj = childrenArray.getJSONObject(j);
                                    String childStudentUserId = childObj.getString("student_userid");
                                    String relation = childObj.getString("relation");
                                    String name = childObj.getString("name");
                                    // 委托给 Service 层创建家长 - 孩子关系记录
                                    parentStudentRelationService.createAndSaveParentStudentRelation(parentUserId, childStudentUserId, name, relation, mobile, externalUserid);
                                }
                            }
                        }
                        // 委托给 Service 层删除不再存在的家长绑定记录
                        departmentParentBindingService.deleteObsoleteParentBindings(existingBindings, currentParentUserIds, targetDepartmentId);
                    } else {
                        logger.info("部门 ID {} 的家长列表为空", targetDepartmentId);
                    }
                } else {
                    logger.error("获取部门 ID {} 的家长列表失败：{}", targetDepartmentId, parentJson.getString("errmsg"));
                }
            }
        } catch (Exception e) {
            logger.error("同步家长学生关系数据失败", e);
        } finally {
            // 确保执行完成后释放锁
            isExecuting.set(false);
        }
        logger.info("家长学生关系同步任务执行完成");
    }
}