package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.core.page.TableDataInfo;
import com.sp.common.enums.BusinessType;
import com.sp.common.exception.DuplicateSubmissionException;
import com.sp.common.utils.ResponseUtils;
import com.sp.framework.interceptor.TokenInterceptor;
import com.sp.system.entity.Notification;
import com.sp.system.entity.vo.NotificationDetailVO;
import com.sp.system.entity.vo.NotificationWithReadStatusVO;
import com.sp.system.entity.vo.SubmitAnswersVO;
import com.sp.system.service.INotificationAnswerService;
import com.sp.system.service.INotificationService;
import com.sp.system.service.INotificationUserReadRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家校通知 Controller
 * <p>
 * userId 由 TokenInterceptor 解析後注入 request attribute。
 * 涉及學生維度的接口需傳入學籍 student_id（非 student_user_id）。
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

    /** 從 request attribute 取得已驗證的 userId */
    private String getUserId() {
        return (String) getRequest().getAttribute(TokenInterceptor.USER_ID_ATTR);
    }

    /**
     * 分頁查詢已發布通知列表（附帶閱讀狀態）
     *
     * @param studentId 學籍 student_id，可選；不傳則查全部學生
     */
    @Log(title = "查詢已發布的通知列表", businessType = BusinessType.SELECT)
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(value = "studentId", required = false) String studentId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }

            startPage();
            List<NotificationWithReadStatusVO> notifications =
                    notificationUserReadRecordService.getPublishedNotificationsForUser(userId, studentId);
            return getDataTable(notifications);
        } catch (Exception e) {
            logger.error("獲取通知列表失敗: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    /**
     * 獲取未讀通知數量
     *
     * @param studentId 學籍 student_id，可選
     */
    @Log(title = "獲取未讀通知數量", businessType = BusinessType.SELECT)
    @GetMapping("/unreadCount")
    public AjaxResult getUnreadCount(@RequestParam(value = "studentId", required = false) String studentId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }

            int unreadCount = notificationUserReadRecordService.countUnreadNotificationsForUser(userId, studentId);
            Map<String, Object> result = new HashMap<>();
            result.put("unreadCount", unreadCount);
            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("獲取未讀通知數量失敗: {}", e.getMessage());
            return AjaxResult.error("獲取未讀通知數量失敗: " + e.getMessage());
        }
    }

    /**
     * 查詢通知詳情（含問題列表與指定學生的作答狀態）
     *
     * @param notificationId 通知 ID
     * @param studentId      學籍 student_id，必填
     */
    @Log(title = "查詢通知詳情", businessType = BusinessType.SELECT)
    @GetMapping("/{notificationId}")
    public AjaxResult getInfo(
            @PathVariable("notificationId") Long notificationId,
            @RequestParam(value = "studentId", required = false) String studentId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }

            // 必須傳遞studentId參數
            if (studentId == null || studentId.isEmpty()) {
                return AjaxResult.error("請指定學生ID");
            }

            // 獲取通知詳情
            NotificationDetailVO detail = notificationService.selectNotificationDetailForStudent(notificationId, studentId);

            if (detail == null || detail.getNotification() == null) {
                return AjaxResult.error("通知不存在");
            }

            if (!"1".equals(detail.getNotification().getStatus())) {
                // status='1' 表示已發布
                return AjaxResult.error("通知不存在或已被撤回");
            }

            return AjaxResult.success(detail);
        } catch (Exception e) {
            logger.error("獲取通知詳情失敗: {}", e.getMessage());
            return AjaxResult.error("獲取通知詳情失敗: " + e.getMessage());
        }
    }

    /**
     * 標記通知為已讀
     *
     * @param notificationId 通知 ID
     * @param studentId      學籍 student_id，必填
     */
    @Log(title = "標記通知爲已讀", businessType = BusinessType.UPDATE)
    @PostMapping("/{notificationId}/read")
    public AjaxResult markAsRead(
            @PathVariable("notificationId") Long notificationId,
            @RequestParam(value = "studentId", required = false) String studentId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }

            // 必須傳遞studentId參數
            if (studentId == null || studentId.isEmpty()) {
                return AjaxResult.error("請指定學生ID");
            }
            
            notificationUserReadRecordService.markAsRead(notificationId, userId, studentId);
            return AjaxResult.success("已標記爲已讀");
        } catch (Exception e) {
            logger.error("標記已讀失敗: {}", e.getMessage());
            return AjaxResult.error("標記已讀失敗: " + e.getMessage());
        }
    }

    /**
     * 提交通知回答（單條），並標記為已回覆
     *
     * @param notificationId  通知 ID
     * @param submitAnswersVO 回答內容，需包含 studentId 與 answer
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
            
            // 必須傳遞studentId參數
            String studentId = submitAnswersVO.getStudentId();
            if (studentId == null || studentId.isEmpty()) {
                return AjaxResult.error("請指定學生ID");
            }

            // 檢查通知是否已發佈且未撤回
            NotificationDetailVO detail = notificationService.selectNotificationDetail(notificationId);
            if (detail == null || detail.getNotification() == null) {
                return AjaxResult.error("通知不存在");
            }
            Notification notification = detail.getNotification();
            if (!"1".equals(notification.getStatus())) {
                // status='1' 表示已發布，非已發布不允許提交
                return AjaxResult.error("通知不存在或已被撤回");
            }

            int count = notificationAnswerService.submitAnswers(submitAnswersVO.getAnswer(), userId, studentId);

            // 標記爲已回復（傳入studentId）
            notificationUserReadRecordService.markAsReplied(notificationId, userId, studentId);
            logger.info("用戶 {} 提交通知 {} 的回答，學生ID: {}", userId, notificationId, studentId);

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
