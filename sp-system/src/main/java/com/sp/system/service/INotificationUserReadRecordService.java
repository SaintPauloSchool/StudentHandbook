package com.sp.system.service;

import com.sp.system.entity.vo.NotificationWithReadStatusVO;

import java.util.List;

/**
 * 通知用戶閱讀記錄 Service 接口
 * <p>
 * 負責與 {@code notification_user_read_record} 及關聯表相關的業務邏輯：
 * <ul>
 *   <li>查詢當前用戶的通知列表（含閱讀狀態）</li>
 *   <li>標記通知爲已讀</li>
 * </ul>
 */
public interface INotificationUserReadRecordService {

    /**
     * 查詢發送給當前用戶的已發布通知列表（附帶閱讀狀態）
     * <p>
     * 分頁由調用方通過 PageHelper（startPage）控制。
     *
     * @param userId    當前家長用戶ID
     * @param studentId 學生ID（可爲null）
     * @return 通知列表（含 isRead、readId、sendRecordId 字段）
     */
    List<NotificationWithReadStatusVO> getPublishedNotificationsForUser(String userId, String studentId);

    /**
     * 查詢發送給當前用戶的未讀通知數量
     *
     * @param userId 當前家長用戶ID
     * @param studentId 學籍 student_id（可爲null）
     * @return 未讀數量
     */
    int countUnreadNotificationsForUser(String userId, String studentId);

    /**
     * 將指定通知對當前用戶標記爲已讀
     * <p>
     * 若該用戶的閱讀記錄存在且爲未讀（is_read='0'），則更新爲已讀並記錄閱讀時間；
     * 若記錄不存在或已是已讀狀態，則忽略。
     *
     * @param notificationId 通知ID
     * @param userId         當前家長用戶ID
     * @param studentId  學籍 student_id（可爲null）
     */
    void markAsRead(Long notificationId, String userId, String studentId);

    /**
     * 將指定通知對當前用戶標記爲已回復
     * <p>
     * 若該用戶的閱讀記錄存在且爲未回復（reply_status='0'），則更新爲已回復並記錄回復時間；
     * 若記錄不存在或已回復，則忽略。
     *
     * @param notificationId 通知ID
     * @param userId         當前家長用戶ID
     * @param studentId  學籍 student_id
     */
    void markAsReplied(Long notificationId, String userId, String studentId);
}
