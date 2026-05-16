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
 * 学校通知定时发送任务
 * 每周一到周五下午6点执行学校通知发送任务（北京时间）
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
     * 每批发送的最大家长数量（企业微信限制）
     */
    private static final int BATCH_SIZE = 1000;
    /**
     * 每周一到周五下午6点执行学校通知发送任务（北京时间）
     */
    @Scheduled(cron = "0 0 18 ? * MON-FRI", zone = "Asia/Shanghai")
    public void sendSchoolNotice() {
        // 使用AtomicBoolean确保同一时间只有一个实例在执行
        if (!isExecuting.compareAndSet(false, true)) {
            logger.info("学校通知发送任务已在执行中，跳过本次执行");
            return;
        }

        try {
            logger.info("开始执行定时学校通知发送任务");

            // 从sys_department_parent_binding表获取parent_user_id列表
            List<String> allParentUserIds = departmentParentBindingService.getAllParentUserIds();
            if (allParentUserIds == null) {
                allParentUserIds = new ArrayList<>();
            }
            logger.info("获取到家长用户ID总数量: {}", allParentUserIds.size());

            if (allParentUserIds.isEmpty()) {
                logger.warn("没有家长用户ID，跳过发送");
                return;
            }

            // 获取access_token
            String accessToken = weChatWorkSchoolUtils.getAccessToken();
            logger.info("成功获取access_token");

            // 构造请求URL
            String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/message/send?access_token=" + accessToken;

            // 分批发送消息
            int totalBatches = (int) Math.ceil((double) allParentUserIds.size() / BATCH_SIZE);
            logger.info("需要分 {} 批发送，每批最多 {} 个家长", totalBatches, BATCH_SIZE);

            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < allParentUserIds.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, allParentUserIds.size());
                List<String> batchList = allParentUserIds.subList(i, endIndex);
                int batchNumber = (i / BATCH_SIZE) + 1;

                logger.info("开始发送第 {}/{} 批，本批家长数量: {}", batchNumber, totalBatches, batchList.size());

                // 构造请求参数
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

                // 发送POST请求
                String requestBody = JSON.toJSONString(noticeRequest);
                String response = HttpUtils.sendPost(url, requestBody);

                // 解析响应结果
                JSONObject result = JSONObject.parseObject(response);
                int errcode = result.getInteger("errcode");
                String errmsg = result.getString("errmsg");

                if (errcode == 0) {
                    successCount++;
                    logger.info("第 {}/{} 批发送成功", batchNumber, totalBatches);
                } else {
                    failCount++;
                    logger.error("第 {}/{} 批发送失败: errcode={}, errmsg={}", batchNumber, totalBatches, errcode, errmsg);
                }
            }

            logger.info("学校通知发送完成 - 总批次: {}, 成功: {}, 失败: {}", totalBatches, successCount, failCount);
        } catch (Exception e) {
            logger.error("定时发送学校通知失败", e);
        } finally {
            // 确保执行完成后释放锁
            isExecuting.set(false);
        }

        logger.info("定时学校通知发送任务执行完成");
    }
}