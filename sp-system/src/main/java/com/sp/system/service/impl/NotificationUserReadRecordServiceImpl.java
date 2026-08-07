package com.sp.system.service.impl;

import com.sp.system.entity.NotificationUserReadRecord;
import com.sp.system.entity.vo.NotificationWithReadStatusVO;
import com.sp.system.mapper.NotificationUserReadRecordMapper;
import com.sp.system.service.INotificationUserReadRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知用戶閱讀記錄 Service 實現
 * <p>
 * 以 notification_user_read_record 為驅動，查詢發送給家長的通知及閱讀/回覆狀態。
 * 閱讀記錄在通知發送時創建，標記已讀/已回覆僅做更新。
 */
@Service
public class NotificationUserReadRecordServiceImpl implements INotificationUserReadRecordService {

    @Autowired
    private NotificationUserReadRecordMapper notificationUserReadRecordMapper;

    /**
     * 查詢發送給當前家長的已發布通知列表（附帶閱讀狀態）
     * <p>
     * 分頁由調用方通過 PageHelper 控制；studentId 為空時不過濾學生。
     *
     * @param userId    家長 user_id
     * @param studentId 學籍 student_id，可選
     */
    @Override
    public List<NotificationWithReadStatusVO> getPublishedNotificationsForUser(String userId, String studentId) {
        return notificationUserReadRecordMapper.selectPublishedNotificationsForUser(userId, studentId);
    }

    /**
     * 統計未讀通知數量
     *
     * @param userId    家長 user_id
     * @param studentId 學籍 student_id，可選；為空時統計全部學生
     */
    @Override
    public int countUnreadNotificationsForUser(String userId, String studentId) {
        return notificationUserReadRecordMapper.countUnreadNotificationsForUser(userId, studentId);
    }

    /**
     * 將指定通知對當前用戶標記爲已讀
     * <p>
     * 查詢對應的閱讀記錄，若存在且爲未讀則更新；否則忽略
     * （閱讀記錄由發送時創建，此處不自動插入）。
     */
    @Override
    public void markAsRead(Long notificationId, String userId, String studentId) {
        // 查詢對應的閱讀記錄
        NotificationUserReadRecord record =
                notificationUserReadRecordMapper.selectByNotificationAndUser(notificationId, userId, studentId);
        // 若存在且爲未讀則更新
        if (record != null && "0".equals(record.getIsRead())) {
            notificationUserReadRecordMapper.markAsRead(record.getReadId(), LocalDateTime.now());
        }
    }

    /**
     * 將指定通知對當前用戶標記爲已回復
     * <p>
     * 查詢對應的閱讀記錄，若存在且爲未回復則更新 reply_status='1' 與 reply_time；否則忽略。
     */
    @Override
    public void markAsReplied(Long notificationId, String userId, String studentId) {
        // 查詢對應的閱讀記錄（根據userId和studentId）
        NotificationUserReadRecord record =
                notificationUserReadRecordMapper.selectByNotificationAndUser(notificationId, userId, studentId);
        // 若存在且爲未回復則更新
        if (record != null && "0".equals(record.getReplyStatus())) {
            notificationUserReadRecordMapper.markAsReplied(record.getReadId(), LocalDateTime.now());
        }
    }
}
