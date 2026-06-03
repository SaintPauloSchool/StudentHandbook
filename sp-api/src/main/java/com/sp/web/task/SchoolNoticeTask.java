package com.sp.web.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sp.common.utils.WeChatWorkSchoolUtils;
import com.sp.common.utils.http.HttpUtils;
import com.sp.system.service.DepartmentParentBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 學校通知定時發送任務
 * 每周一到周五下午6點執行學校通知發送任務（北京時間）
 */
@Component
public class SchoolNoticeTask {

    private static final Logger logger = LoggerFactory.getLogger(SchoolNoticeTask.class);

    @Autowired
    private WeChatWorkSchoolUtils weChatWorkSchoolUtils;

    @Autowired
    private DepartmentParentBindingService departmentParentBindingService;

    private static final AtomicBoolean isExecuting = new AtomicBoolean(false);

    /**
     * 每批發送的最大家長數量（企業微信限制）
     */
    private static final int BATCH_SIZE = 1000;
    /**
     * 每周一到周五下午6點執行學校通知發送任務（北京時間）
     */
    //@Scheduled(cron = "0 0 18 ? * MON-FRI", zone = "Asia/Shanghai")
    public void sendSchoolNotice() {
        // 使用AtomicBoolean確保同一時間只有一個實例在執行
        if (!isExecuting.compareAndSet(false, true)) {
            logger.info("學校通知發送任務已在執行中，跳過本次執行");
            return;
        }

        try {
            logger.info("開始執行定時學校通知發送任務");

            // 從sys_department_parent_binding表獲取parent_user_id列表
            List<String> allParentUserIds = departmentParentBindingService.getAllParentUserIds();
            if (allParentUserIds == null) {
                allParentUserIds = new ArrayList<>();
            }
            logger.info("獲取到家長用戶ID總數量: {}", allParentUserIds.size());

            if (allParentUserIds.isEmpty()) {
                logger.warn("沒有家長用戶ID，跳過發送");
                return;
            }

            // 獲取access_token
            String accessToken = weChatWorkSchoolUtils.getAccessToken();
            logger.info("成功獲取access_token");

            // 構造請求URL
            String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/message/send?access_token=" + accessToken;

            // 分批發送消息
            int totalBatches = (int) Math.ceil((double) allParentUserIds.size() / BATCH_SIZE);
            logger.info("需要分 {} 批發送，每批最多 {} 個家長", totalBatches, BATCH_SIZE);

            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < allParentUserIds.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, allParentUserIds.size());
                List<String> batchList = allParentUserIds.subList(i, endIndex);
                int batchNumber = (i / BATCH_SIZE) + 1;

                logger.info("開始發送第 {}/{} 批，本批家長數量: {}", batchNumber, totalBatches, batchList.size());

                // 構造請求參數
                Map<String, Object> noticeRequest = new HashMap<>();
                noticeRequest.put("recv_scope", 0);
                noticeRequest.put("to_parent_userid", batchList);
                noticeRequest.put("to_student_userid", new ArrayList<>());
                noticeRequest.put("to_party", new ArrayList<>());
                noticeRequest.put("toall", 0);
                noticeRequest.put("msgtype", "text");
                noticeRequest.put("agentid", 1000033);

                Map<String, String> textContent = new HashMap<>();
                textContent.put("content",
                    "📚 今日學生手冊\n\n" +
                    "🔗 請點擊以下連接查看今日學生手冊：\n" +
                    "https://mo-stu-sys.org-assistant.com/handbook");
                noticeRequest.put("text", textContent);

                noticeRequest.put("enable_id_trans", 0);
                noticeRequest.put("enable_duplicate_check", 0);
                noticeRequest.put("duplicate_check_interval", 1800);

                // 發送POST請求
                String requestBody = JSON.toJSONString(noticeRequest);
                String response = HttpUtils.sendPost(url, requestBody);

                // 解析響應結果
                JSONObject result = JSONObject.parseObject(response);
                int errcode = result.getInteger("errcode");
                String errmsg = result.getString("errmsg");

                if (errcode == 0) {
                    successCount++;
                    logger.info("第 {}/{} 批發送成功", batchNumber, totalBatches);
                } else {
                    failCount++;
                    logger.error("第 {}/{} 批發送失敗: errcode={}, errmsg={}", batchNumber, totalBatches, errcode, errmsg);
                }
            }

            logger.info("學校通知發送完成 - 總批次: {}, 成功: {}, 失敗: {}", totalBatches, successCount, failCount);
        } catch (Exception e) {
            logger.error("定時發送學校通知失敗", e);
        } finally {
            // 確保執行完成後釋放鎖
            isExecuting.set(false);
        }

        logger.info("定時學校通知發送任務執行完成");
    }
}