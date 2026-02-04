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

            // 构造请求参数
            Map<String, Object> noticeRequest = new HashMap<>();
            noticeRequest.put("recv_scope", 0);

            // 从sys_department_parent_binding表获取parent_user_id列表
            List<String> toParentUserId = departmentParentBindingService.getAllParentUserIds();

            noticeRequest.put("to_parent_userid", toParentUserId);
            noticeRequest.put("to_student_userid", new ArrayList<>());


            noticeRequest.put("to_party", new ArrayList<>());
            noticeRequest.put("toall", 0);
            noticeRequest.put("msgtype", "text");
            noticeRequest.put("agentid", 1000033);

            Map<String, String> textContent = new HashMap<>();
            textContent.put("content", "請點擊連接查看今日學生手冊：https://mo-stu-sys.org-assistant.com/sp-api/");
            noticeRequest.put("text", textContent);

            noticeRequest.put("enable_id_trans", 0);
            noticeRequest.put("enable_duplicate_check", 0);
            noticeRequest.put("duplicate_check_interval", 1800);

            logger.info("构造通知参数: {}", JSON.toJSONString(noticeRequest));

            // 获取access_token
            String accessToken = weChatWorkSchoolUtils.getAccessToken();
            logger.info("成功获取access_token");

            // 构造请求URL
            String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/message/send?access_token=" + accessToken;

            // 发送POST请求
            String response = HttpUtils.sendPost(url, JSON.toJSONString(noticeRequest));
            logger.info("定时发送学校通知完成，响应结果: {}", response);

            // 解析响应结果
            JSONObject result = JSONObject.parseObject(response);
            logger.info("学校通知发送结果: errcode={}, errmsg={}", result.getInteger("errcode"), result.getString("errmsg"));
        } catch (Exception e) {
            logger.error("定时发送学校通知失败", e);
        } finally {
            // 确保执行完成后释放锁
            isExecuting.set(false);
        }

        logger.info("定时学校通知发送任务执行完成");
    }
}