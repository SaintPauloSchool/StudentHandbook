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
 * 企業微信獲取部門列表定時任務
 * 每天凌晨 1 點執行，調用企業微信接口獲取部門和成員數據並同步到數據庫（以企業微信數據為準，進行增刪改）
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
     * 每天凌晨 1 點執行（北京時間）
     */
    //@Scheduled(cron = "0 0 1 * * ?", zone = "Asia/Shanghai")
    public void syncWecomSchoolDepartments() {
        if (!isExecuting.compareAndSet(false, true)) {
            logger.info("任務已在執行中，跳過本次執行");
            return;
        }

        try {
            logger.info("開始執行定時任務 - 同步部門和成員數據");
            // 獲取 access_token
            String accessToken = weChatWorkSchoolUtils.getAccessToken();
            // 獲取並同步部門數據
            JSONArray departmentIdArray = wecomSchoolDepartmentService.fetchAndSaveAllDepartments(accessToken);
            // 部門數據如果爲空
            if (departmentIdArray == null || departmentIdArray.isEmpty()) {
                logger.warn("未獲取到部門數據");
                return;
            }
            //
            logger.info("成功同步 {} 個部門", departmentIdArray.size());
            // 獲取並同步成員數據
            int totalMembers = wecomSchoolDepartmentMemberService.fetchAndSaveAllMembers(departmentIdArray, accessToken);
            //
            logger.info("總共同步 {} 個成員", totalMembers);
            logger.info("定時任務執行完成");
        } catch (Exception e) {
            logger.error("執行定時任務失敗", e);
        } finally {
            isExecuting.set(false);
        }
    }

}
