package com.sp.web.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sp.common.utils.WeChatWorkSchoolUtils;
import com.sp.common.utils.http.HttpUtils;
import com.sp.system.entity.Department;
import com.sp.system.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 部门数据同步定时任务
 * 每天凌晨12点同步企业微信部门数据
 */
@Component
public class DepartmentSyncTask {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentSyncTask.class);

    @Autowired
    private WeChatWorkSchoolUtils weChatWorkSchoolUtils;

    @Autowired
    private DepartmentService departmentService;

    private static final AtomicBoolean isExecuting = new AtomicBoolean(false);

    /**
     * 每天凌晨12点执行部门数据同步
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void syncDepartmentData() {
        // 使用AtomicBoolean确保同一时间只有一个实例在执行
        if (!isExecuting.compareAndSet(false, true)) {
            logger.info("部门数据同步任务已在执行中，跳过本次执行");
            return;
        }
        
        try {
            logger.info("开始执行部门数据同步任务");

            // 获取access_token并调用部门列表接口
            String accessToken = weChatWorkSchoolUtils.getAccessToken();
            String departmentListUrl = "https://qyapi.weixin.qq.com/cgi-bin/school/department/list?access_token=" + accessToken;

            String departmentResponse = HttpUtils.sendGet(departmentListUrl);
            logger.info("获取企业微信部门列表结果: {}", departmentResponse);

            // 解析部门列表并保存到数据库
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

                    // 批量保存部门信息（存在则更新，不存在则新增）
                    departmentService.batchSaveDepartments(departmentsToSave);
                    logger.info("成功同步 {} 个部门信息到数据库", departmentsArray.size());
                } else {
                    logger.info("部门列表为空");
                }
            } else {
                logger.error("获取部门列表失败: {}", departmentJson.getString("errmsg"));
            }
        } catch (Exception e) {
            logger.error("同步部门数据失败", e);
        } finally {
            // 确保执行完成后释放锁
            isExecuting.set(false);
        }

        logger.info("部门数据同步任务执行完成");
    }
}