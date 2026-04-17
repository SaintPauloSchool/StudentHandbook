package com.sp.web.task;

import com.alibaba.fastjson.JSONArray;
import com.sp.common.utils.WeChatWorkSchoolUtils;
import com.sp.system.service.WecomSchoolDepartmentService;
import com.sp.system.service.WecomSchoolDepartmentMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 企业微信获取部门列表定时任务
 * 每天凌晨 1 点执行，调用企业微信接口获取部门和成员数据并同步到数据库（以企业微信数据为准，进行增删改）
 */
@Component
public class WecomSchoolDepartmentTask {

    private static final Logger logger = LoggerFactory.getLogger(WecomSchoolDepartmentTask.class);

    @Autowired
    private WeChatWorkSchoolUtils weChatWorkSchoolUtils;

    @Autowired
    private WecomSchoolDepartmentService wecomSchoolDepartmentService;

    @Autowired
    private WecomSchoolDepartmentMemberService wecomSchoolDepartmentMemberService;

    private static final AtomicBoolean isExecuting = new AtomicBoolean(false);

    /**
     * 每天凌晨 1 点执行（北京时间）
     */
    //@Scheduled(cron = "0 0 1 * * ?", zone = "Asia/Shanghai")
    public void syncWecomSchoolDepartments() {
        if (!isExecuting.compareAndSet(false, true)) {
            logger.info("任务已在执行中，跳过本次执行");
            return;
        }

        try {
            logger.info("开始执行定时任务 - 同步部门和成员数据");
            // 获取 access_token
            String accessToken = weChatWorkSchoolUtils.getAccessToken();
            // 获取并同步部门数据
            JSONArray departmentIdArray = wecomSchoolDepartmentService.fetchAndSaveAllDepartments(accessToken);
            // 部门数据如果为空
            if (departmentIdArray == null || departmentIdArray.isEmpty()) {
                logger.warn("未获取到部门数据");
                return;
            }
            //
            logger.info("成功同步 {} 个部门", departmentIdArray.size());
            // 获取并同步成员数据
            int totalMembers = wecomSchoolDepartmentMemberService.fetchAndSaveAllMembers(departmentIdArray, accessToken);
            //
            logger.info("总共同步 {} 个成员", totalMembers);
            logger.info("定时任务执行完成");
        } catch (Exception e) {
            logger.error("执行定时任务失败", e);
        } finally {
            isExecuting.set(false);
        }
    }

}
