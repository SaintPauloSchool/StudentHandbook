package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.enums.BusinessType;
import com.sp.common.exception.DuplicateSubmissionException;
import com.sp.framework.interceptor.TokenInterceptor;
import com.sp.system.entity.NotificationAnswer;
import com.sp.system.entity.vo.NotificationWithReadStatusVO;
import com.sp.system.entity.vo.SubmitAnswersVO;
import com.sp.system.service.INotificationAnswerService;
import com.sp.system.service.INotificationService;
import com.sp.system.service.INotificationUserReadRecordService;
import com.sp.system.service.IParentStudentRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家校通知Controller
 * userId 由 TokenInterceptor 解析後注入 request attribute，Controller 直接取用
 */
@RestController
@RequestMapping("/system/notice")
public class ParentNoticeController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ParentNoticeController.class);

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private INotificationUserReadRecordService notificationUserReadRecordService;

    @Autowired
    private INotificationAnswerService notificationAnswerService;

    @Autowired
    private IParentStudentRelationService parentStudentRelationService;

    /** 從 request attribute 取得已驗證的 userId */
    private String getUserId() {
        return (String) getRequest().getAttribute(TokenInterceptor.USER_ID_ATTR);
    }

    /**
     * 查询发送给当前家长的已发布通知列表（附带阅读状态）
     */
    @Log(title = "查询已发布的通知列表", businessType = BusinessType.SELECT)
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "studentUserId", required = false) String studentUserId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            List<NotificationWithReadStatusVO> notifications =
                    notificationUserReadRecordService.getPublishedNotificationsForUser(pageNum, pageSize, userId, studentUserId);
            int total = notificationUserReadRecordService.countPublishedNotificationsForUser(userId, studentUserId);

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
     * 获取当前用户的未读通知数量
     */
    @Log(title = "获取未读通知数量", businessType = BusinessType.SELECT)
    @GetMapping("/unreadCount")
    public AjaxResult getUnreadCount(@RequestParam(value = "studentUserId", required = false) String studentUserId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            int unreadCount = notificationUserReadRecordService.countUnreadNotificationsForUser(userId, studentUserId);
            Map<String, Object> result = new HashMap<>();
            result.put("unreadCount", unreadCount);
            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("获取未读通知数量失败: {}", e.getMessage());
            return AjaxResult.error("获取未读通知数量失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询通知详情（包含问题列表和用户答案）
     */
    @Log(title = "查询通知详情", businessType = BusinessType.SELECT)
    @GetMapping("/{notificationId}")
    public AjaxResult getInfo(@PathVariable("notificationId") Long notificationId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            Map<String, Object> result = notificationService.selectNotificationDetail(notificationId);

            if (result == null || result.get("notification") == null) {
                return AjaxResult.error("通知不存在");
            }

            String studentUserId = notificationAnswerService.getStudentUserIdByParentId(userId);
            NotificationAnswer userAnswer = notificationAnswerService.getUserAnswer(notificationId, studentUserId);

            if (userAnswer != null) {
                String answererInfo = parentStudentRelationService.getAnswererInfo(userAnswer.getUserId());
                result.put("userAnswer", userAnswer);
                result.put("answererInfo", answererInfo);
                result.put("hasSubmitted", true);
            } else {
                result.put("userAnswer", null);
                result.put("answererInfo", "");
                result.put("hasSubmitted", false);
            }

            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("获取通知详情失败: {}", e.getMessage());
            return AjaxResult.error("获取通知详情失败: " + e.getMessage());
        }
    }

    /**
     * 标记通知为已读
     */
    @Log(title = "标记通知为已读", businessType = BusinessType.UPDATE)
    @PostMapping("/{notificationId}/read")
    public AjaxResult markAsRead(@PathVariable("notificationId") Long notificationId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }
            notificationUserReadRecordService.markAsRead(notificationId, userId);
            return AjaxResult.success("已标记为已读");
        } catch (Exception e) {
            logger.error("标记已读失败: {}", e.getMessage());
            return AjaxResult.error("标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 提交通知回答
     */
    @Log(title = "提交通知回答", businessType = BusinessType.INSERT)
    @PostMapping("/{notificationId}/submit")
    public AjaxResult submitAnswers(
            @PathVariable("notificationId") Long notificationId,
            @RequestBody SubmitAnswersVO submitAnswersVO) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            if (submitAnswersVO == null || submitAnswersVO.getAnswer() == null) {
                return AjaxResult.error("请至少回答一个问题");
            }

            String userType = "2"; // 2表示家长
            int count = notificationAnswerService.submitAnswers(submitAnswersVO.getAnswer(), userId, userType);

            notificationUserReadRecordService.markAsReplied(notificationId, userId);
            logger.info("用户 {} 提交通知 {} 的回答", userId, notificationId);

            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return AjaxResult.success("提交成功", result);
        } catch (DuplicateSubmissionException e) {
            return AjaxResult.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("提交通知回答失败: {}", e.getMessage(), e);
            return AjaxResult.error("提交失败: " + e.getMessage());
        }
    }
}
