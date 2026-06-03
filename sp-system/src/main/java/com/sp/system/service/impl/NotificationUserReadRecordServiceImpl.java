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
 * 通知用戶閱讀記錄 Service 實現類
 */
@Service
public class NotificationUserReadRecordServiceImpl implements INotificationUserReadRecordService {

    @Autowired
    private NotificationUserReadRecordMapper notificationUserReadRecordMapper;

    /**
     * 分頁查詢發送給當前用戶的已發布通知列表（附帶閱讀狀態）
     */
    @Override
    public List<NotificationWithReadStatusVO> getPublishedNotificationsForUser(int pageNum, int pageSize, String userId, String studentUserId) {
        int offset = (pageNum - 1) * pageSize;
        return notificationUserReadRecordMapper.selectPublishedNotificationsForUser(offset, pageSize, userId, studentUserId);
    }

    /**
     * 查詢發送給當前用戶的已發布通知總數
     */
    @Override
    public int countPublishedNotificationsForUser(String userId, String studentUserId) {
        return notificationUserReadRecordMapper.countPublishedNotificationsForUser(userId, studentUserId);
    }

    /**
     * 查詢發送給當前用戶的未讀通知數量
     */
    @Override
    public int countUnreadNotificationsForUser(String userId, String studentUserId) {
        return notificationUserReadRecordMapper.countUnreadNotificationsForUser(userId, studentUserId);
    }

    /**
     * 將指定通知對當前用戶標記爲已讀
     * <p>
     * 查詢對應的閱讀記錄，若存在且爲未讀則更新；否則忽略
     * （閱讀記錄由發送時創建，此處不自動插入）。
     */
    @Override
    public void markAsRead(Long notificationId, String userId, String studentUserId) {
        // 查詢對應的閱讀記錄
        NotificationUserReadRecord record =
                notificationUserReadRecordMapper.selectByNotificationAndUser(notificationId, userId, studentUserId);
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
    public void markAsReplied(Long notificationId, String userId, String studentUserId) {
        // 查詢對應的閱讀記錄（根據userId和studentUserId）
        NotificationUserReadRecord record =
                notificationUserReadRecordMapper.selectByNotificationAndUser(notificationId, userId, studentUserId);
        // 若存在且爲未回復則更新
        if (record != null && "0".equals(record.getReplyStatus())) {
            notificationUserReadRecordMapper.markAsReplied(record.getReadId(), LocalDateTime.now());
        }
    }
}
