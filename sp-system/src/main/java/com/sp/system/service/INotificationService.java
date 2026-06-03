package com.sp.system.service;

import java.util.Map;

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
     * @return 包含 notification 和 questions 的 Map；若通知不存在則返回 null
     */
    Map<String, Object> selectNotificationDetail(Long notificationId);
}
