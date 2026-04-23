package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.enums.BusinessType;
import com.sp.system.entity.Notification;
import com.sp.system.entity.NotificationAnswer;
import com.sp.system.service.INotificationAnswerService;
import com.sp.system.service.INotificationService;
import com.sp.system.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家校通知Controller
 */
@RestController
@RequestMapping("/system/notice")
public class ParentNoticeController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ParentNoticeController.class);

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private INotificationAnswerService notificationAnswerService;

    /**
     * 查询已发布的通知列表
     */
    @Log(title = "查询已发布的通知列表", businessType = BusinessType.SELECT)
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            // 分页查询通知列表
            List<Notification> notifications = notificationService.selectPublishedNotificationsPage(pageNum, pageSize);
            
            // 查询总数
            int total = notificationService.countPublishedNotifications();
            
            // 返回分页数据
            Map<String, Object> result = new HashMap<>();
            result.put("list", notifications);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", (int) Math.ceil((double) total / pageSize));
            
            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("获取通知列表失败: {}", e.getMessage());
            return AjaxResult.error("获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询通知详情（包含问题列表）
     */
    @Log(title = "查询通知详情", businessType = BusinessType.SELECT)
    @GetMapping("/{notificationId}")
    public AjaxResult getInfo(@PathVariable("notificationId") Long notificationId) {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            // 通过Service层查询通知详情（包含问题列表）
            Map<String, Object> result = notificationService.selectNotificationDetail(notificationId);
            
            if (result == null || result.get("notification") == null) {
                return AjaxResult.error("通知不存在");
            }

            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("获取通知详情失败: {}", e.getMessage());
            return AjaxResult.error("获取通知详情失败: " + e.getMessage());
        }
    }

    /**
     * 提交通知回答
     */
    @Log(title = "提交通知回答", businessType = BusinessType.INSERT)
    @PostMapping("/{notificationId}/submit")
    public AjaxResult submitAnswers(
            @PathVariable("notificationId") Long notificationId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            // 获取答案列表
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> answersData = (List<Map<String, Object>>) requestBody.get("answers");
            
            if (answersData == null || answersData.isEmpty()) {
                return AjaxResult.error("请至少回答一个问题");
            }

            // 转换答案数据
            List<NotificationAnswer> answers = convertToNotificationAnswers(answersData, parentUserId);
            
            // 批量保存答案
            int count = notificationAnswerService.batchSaveAnswers(answers);
            
            logger.info("用户 {} 提交通知 {} 的回答，共 {} 条", parentUserId, notificationId, count);
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return AjaxResult.success("提交成功", result);
        } catch (Exception e) {
            logger.error("提交通知回答失败: {}", e.getMessage(), e);
            return AjaxResult.error("提交失败: " + e.getMessage());
        }
    }

    /**
     * 将前端传来的答案数据转换为实体对象
     */
    private List<NotificationAnswer> convertToNotificationAnswers(List<Map<String, Object>> answersData, String parentUserId) {
        // TODO: 这里需要根据实际的user_id转换逻辑来实现
        // 目前先使用parentUserId作为userId，实际项目中需要从token或数据库中获取真正的userId
        
        return answersData.stream().map(data -> {
            NotificationAnswer answer = new NotificationAnswer();
            
            // 设置通知ID
            if (data.get("notificationId") != null) {
                answer.setNotificationId(Long.valueOf(data.get("notificationId").toString()));
            }
            
            // 设置问题ID
            if (data.get("questionId") != null) {
                answer.setQuestionId(Long.valueOf(data.get("questionId").toString()));
            }
            
            // 设置节点信息（逻辑表单使用）
            answer.setNodeId((String) data.get("nodeId"));
            answer.setNodeTitle((String) data.get("nodeTitle"));
            answer.setNodeType((String) data.get("nodeType"));
            
            // 设置用户ID和类型
            // TODO: 实际项目中需要从数据库查询真正的userId
            answer.setUserId(1L); // 临时使用1，实际需要转换
            answer.setUserType("2"); // 2表示家长
            
            // 设置答案内容
            answer.setAnswerContent((String) data.get("answerContent"));
            
            // 设置附件URL
            answer.setAttachmentUrls((String) data.get("attachmentUrls"));
            
            return answer;
        }).collect(java.util.stream.Collectors.toList());
    }
}
