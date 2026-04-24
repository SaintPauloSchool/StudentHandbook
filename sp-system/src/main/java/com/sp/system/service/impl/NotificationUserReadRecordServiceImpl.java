package com.sp.system.service.impl;

import com.sp.system.entity.NotificationUserReadRecord;
import com.sp.system.entity.vo.NotificationWithReadStatusVO;
import com.sp.system.mapper.NotificationUserReadRecordMapper;
import com.sp.system.service.INotificationUserReadRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知用户阅读记录 Service 实现类
 */
@Service
public class NotificationUserReadRecordServiceImpl implements INotificationUserReadRecordService {

    @Autowired
    private NotificationUserReadRecordMapper notificationUserReadRecordMapper;

    /**
     * 分页查询发送给当前用户的已发布通知列表（附带阅读状态）
     */
    @Override
    public List<NotificationWithReadStatusVO> getPublishedNotificationsForUser(int pageNum, int pageSize, String userId) {
        int offset = (pageNum - 1) * pageSize;
        return notificationUserReadRecordMapper.selectPublishedNotificationsForUser(offset, pageSize, userId);
    }

    /**
     * 查询发送给当前用户的已发布通知总数
     */
    @Override
    public int countPublishedNotificationsForUser(String userId) {
        return notificationUserReadRecordMapper.countPublishedNotificationsForUser(userId);
    }

    /**
     * 将指定通知对当前用户标记为已读
     * <p>
     * 查询对应的阅读记录，若存在且为未读则更新；否则忽略
     * （阅读记录由发送时创建，此处不自动插入）。
     */
    @Override
    public void markAsRead(Long notificationId, String userId) {
        // 查询对应的阅读记录
        NotificationUserReadRecord record =
                notificationUserReadRecordMapper.selectByNotificationAndUser(notificationId, userId);
        // 若存在且为未读则更新
        if (record != null && "0".equals(record.getIsRead())) {
            notificationUserReadRecordMapper.markAsRead(record.getReadId());
        }
    }

    /**
     * 将指定通知对当前用户标记为已回复
     * <p>
     * 查询对应的阅读记录，若存在且为未回复则更新 reply_status='1' 与 reply_time；否则忽略。
     */
    @Override
    public void markAsReplied(Long notificationId, String userId) {
        // 查询对应的阅读记录
        NotificationUserReadRecord record =
                notificationUserReadRecordMapper.selectByNotificationAndUser(notificationId, userId);
        // 若存在且为未回复则更新
        if (record != null && "0".equals(record.getReplyStatus())) {
            notificationUserReadRecordMapper.markAsReplied(record.getReadId());
        }
    }
}
