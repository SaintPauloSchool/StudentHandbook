package com.sp.system.service;

import com.sp.system.entity.vo.NotificationDetailVO;

/**
 * 通知Service接口
 * <p>
 * 負責通知主體數據的查詢（與用戶無關的通用邏輯）。
 * 與用戶閱讀狀態相關的業務請使用 {@link INotificationUserReadRecordService}。
 */
public interface INotificationService {

    /**
     * 根據ID查詢通知詳情（包含問題列表）
     *
     * @param notificationId 通知ID
     * @return 通知詳情；若通知不存在則返回 null
     */
    NotificationDetailVO selectNotificationDetail(Long notificationId);

    /**
     * 查詢通知詳情，並附帶指定學生的作答情況
     *
     * @param notificationId 通知ID
     * @param studentId        學籍 student_id
     * @return 通知詳情；若通知不存在則返回 null
     */
    NotificationDetailVO selectNotificationDetailForStudent(Long notificationId, String studentId);
}
