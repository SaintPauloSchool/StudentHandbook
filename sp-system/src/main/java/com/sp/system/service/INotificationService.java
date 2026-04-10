package com.sp.system.service;

import com.sp.system.entity.Notification;
import com.sp.system.entity.NotificationQuestion;

import java.util.List;

/**
 * 通知Service接口
 */
public interface INotificationService {
    
    /**
     * 查询已发布的通知列表
     * @return 通知列表
     */
    List<Notification> selectPublishedNotifications();
    
    /**
     * 根据ID查询通知详情（包含问题列表）
     * @param notificationId 通知ID
     * @return 通知详情
     */
    Notification selectNotificationWithQuestions(Long notificationId);
}
