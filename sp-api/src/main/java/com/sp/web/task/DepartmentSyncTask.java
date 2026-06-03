package com.sp.web.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sp.common.utils.WeChatWorkSchoolUtils;
import com.sp.common.utils.http.HttpUtils;
import com.sp.system.entity.Department;
import com.sp.system.entity.DepartmentAdmin;
import com.sp.system.service.DepartmentService;
import com.sp.system.service.DepartmentAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 部門數據同步定時任務
 * 每天凌晨12點同步企業微信部門數據
 */
@Component
public class DepartmentSyncTask {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentSyncTask.class);

    @Autowired
    private WeChatWorkSchoolUtils weChatWorkSchoolUtils;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentAdminService departmentAdminService;

    private static final AtomicBoolean isExecuting = new AtomicBoolean(false);

    /**
     * 每天凌晨12點執行部門數據同步
     */
    //@Scheduled(cron = "0 0 0 * * ?")
    public void syncDepartmentData() {
        // 使用AtomicBoolean確保同一時間只有一個實例在執行
        if (!isExecuting.compareAndSet(false, true)) {
            logger.info("部門數據同步任務已在執行中，跳過本次執行");
            return;
        }
        
        try {
            logger.info("開始執行部門數據同步任務");

            // 獲取access_token並調用部門列表接口
            String accessToken = weChatWorkSchoolUtils.getAccessToken();
            String departmentListUrl = "https://qyapi.weixin.qq.com/cgi-bin/school/department/list?access_token=" + accessToken;

            String departmentResponse = HttpUtils.sendGet(departmentListUrl);
            logger.info("獲取企業微信部門列表結果: {}", departmentResponse);

            // 解析部門列表並保存到數據庫
            JSONObject departmentJson = JSONObject.parseObject(departmentResponse);
            if (departmentJson.getInteger("errcode") != null && departmentJson.getInteger("errcode") == 0) {
                JSONArray departmentsArray = departmentJson.getJSONArray("departments");
                if (departmentsArray != null && !departmentsArray.isEmpty()) {
                    List<Department> departmentsToSave = new ArrayList<>();
                    
                    for (int i = 0; i < departmentsArray.size(); i++) {
                        JSONObject deptObj = departmentsArray.getJSONObject(i);

                        Department department = new Department();
                        department.setId(deptObj.getLong("id"));
                        department.setParentId(deptObj.getInteger("parentid"));
                        department.setName(deptObj.getString("name"));
                        department.setType(deptObj.getInteger("type"));
                        department.setRegisterYear(deptObj.getInteger("register_year"));
                        department.setStandardGrade(deptObj.getInteger("standard_grade"));
                        department.setOrder(deptObj.getInteger("order"));
                        department.setIsGraduated(deptObj.getInteger("is_graduated"));
                        department.setOpenGroupChat(deptObj.getInteger("open_group_chat"));
                        department.setGroupChatId(deptObj.getString("group_chat_id"));

                        departmentsToSave.add(department);
                    }

                    // 批量保存部門信息（存在則更新，不存在則新增）
                    departmentService.batchSaveDepartments(departmentsToSave);
                    logger.info("成功同步 {} 個部門信息到數據庫", departmentsArray.size());

                    // 解析並保存部門管理員信息
                    List<DepartmentAdmin> allAdmins = new ArrayList<>();
                    for (int i = 0; i < departmentsArray.size(); i++) {
                        JSONObject deptObj = departmentsArray.getJSONObject(i);
                        JSONArray adminsArray = deptObj.getJSONArray("department_admins");
                        
                        if (adminsArray != null && !adminsArray.isEmpty()) {
                            Long departmentId = deptObj.getLong("id");
                            
                            for (int j = 0; j < adminsArray.size(); j++) {
                                JSONObject adminObj = adminsArray.getJSONObject(j);
                                
                                DepartmentAdmin admin = new DepartmentAdmin();
                                admin.setDepartmentId(departmentId);
                                admin.setUserid(adminObj.getString("userid"));
                                admin.setType(adminObj.getInteger("type"));
                                admin.setSubject(adminObj.getString("subject"));
                                
                                allAdmins.add(admin);
                            }
                        }
                    }
                    
                    // 批量保存部門管理員信息
                    if (!allAdmins.isEmpty()) {
                        departmentAdminService.batchSaveDepartmentAdmins(allAdmins);
                        logger.info("成功同步 {} 個部門管理員信息到數據庫", allAdmins.size());
                    }
                } else {
                    logger.info("部門列表爲空");
                }
            } else {
                logger.error("獲取部門列表失敗: {}", departmentJson.getString("errmsg"));
            }
        } catch (Exception e) {
            logger.error("同步部門數據失敗", e);
        } finally {
            // 確保執行完成後釋放鎖
            isExecuting.set(false);
        }

        logger.info("部門數據同步任務執行完成");
    }
}