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
     * 查詢發送給當前家長的已發布通知列表（附帶閱讀狀態）
     */
    @Log(title = "查詢已發布的通知列表", businessType = BusinessType.SELECT)
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "studentUserId", required = false) String studentUserId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
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
            logger.error("獲取通知列表失敗: {}", e.getMessage());
            return AjaxResult.error("獲取通知列表失敗: " + e.getMessage());
        }
    }

    /**
     * 獲取當前用戶的未讀通知數量
     */
    @Log(title = "獲取未讀通知數量", businessType = BusinessType.SELECT)
    @GetMapping("/unreadCount")
    public AjaxResult getUnreadCount(@RequestParam(value = "studentUserId", required = false) String studentUserId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }

            int unreadCount = notificationUserReadRecordService.countUnreadNotificationsForUser(userId, studentUserId);
            Map<String, Object> result = new HashMap<>();
            result.put("unreadCount", unreadCount);
            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("獲取未讀通知數量失敗: {}", e.getMessage());
            return AjaxResult.error("獲取未讀通知數量失敗: " + e.getMessage());
        }
    }

    /**
     * 根據ID查詢通知詳情（包含問題列表和用戶答案）
     */
    @Log(title = "查詢通知詳情", businessType = BusinessType.SELECT)
    @GetMapping("/{notificationId}")
    public AjaxResult getInfo(
            @PathVariable("notificationId") Long notificationId,
            @RequestParam(value = "studentUserId", required = false) String studentUserId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }

            // 必須傳遞studentUserId參數
            if (studentUserId == null || studentUserId.isEmpty()) {
                return AjaxResult.error("請指定學生ID");
            }

            // 獲取通知詳情
            Map<String, Object> result = notificationService.selectNotificationDetail(notificationId);

            if (result == null || result.get("notification") == null) {
                return AjaxResult.error("通知不存在");
            }

            // 獲取學生信息
            NotificationAnswer userAnswer = notificationAnswerService.getUserAnswer(notificationId, studentUserId);

            if (userAnswer != null) {
                // 使用回答那條數據的userId(家長ID)和studentUserId獲取作答人信息
                String answererInfo = parentStudentRelationService.getAnswererInfo(userAnswer.getUserId(), studentUserId);
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
            logger.error("獲取通知詳情失敗: {}", e.getMessage());
            return AjaxResult.error("獲取通知詳情失敗: " + e.getMessage());
        }
    }

    /**
     * 標記通知爲已讀
     */
    @Log(title = "標記通知爲已讀", businessType = BusinessType.UPDATE)
    @PostMapping("/{notificationId}/read")
    public AjaxResult markAsRead(
            @PathVariable("notificationId") Long notificationId,
            @RequestParam(value = "studentUserId", required = false) String studentUserId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }

            // 必須傳遞studentUserId參數
            if (studentUserId == null || studentUserId.isEmpty()) {
                return AjaxResult.error("請指定學生ID");
            }
            
            notificationUserReadRecordService.markAsRead(notificationId, userId, studentUserId);
            return AjaxResult.success("已標記爲已讀");
        } catch (Exception e) {
            logger.error("標記已讀失敗: {}", e.getMessage());
            return AjaxResult.error("標記已讀失敗: " + e.getMessage());
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
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }

            if (submitAnswersVO == null || submitAnswersVO.getAnswer() == null) {
                return AjaxResult.error("請至少回答一個問題");
            }
            
            // 必須傳遞studentUserId參數
            String studentUserId = submitAnswersVO.getStudentUserId();
            if (studentUserId == null || studentUserId.isEmpty()) {
                return AjaxResult.error("請指定學生ID");
            }

            String userType = "2"; // 2表示家長
            int count = notificationAnswerService.submitAnswers(submitAnswersVO.getAnswer(), userId, userType, studentUserId);

            // 標記爲已回復（傳入studentUserId）
            notificationUserReadRecordService.markAsReplied(notificationId, userId, studentUserId);
            logger.info("用戶 {} 提交通知 {} 的回答，學生ID: {}", userId, notificationId, studentUserId);

            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return AjaxResult.success("提交成功", result);
        } catch (DuplicateSubmissionException e) {
            return AjaxResult.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("提交通知回答失敗: {}", e.getMessage(), e);
            return AjaxResult.error("提交失敗: " + e.getMessage());
        }
    }
}
