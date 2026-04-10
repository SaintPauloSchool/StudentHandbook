package com.sp.system.service.impl;

import com.sp.system.entity.Notification;
import com.sp.system.entity.NotificationQuestion;
import com.sp.system.mapper.NotificationMapper;
import com.sp.system.mapper.NotificationQuestionMapper;
import com.sp.system.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知Service实现类
 */
@Service
public class NotificationServiceImpl implements INotificationService {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private NotificationQuestionMapper notificationQuestionMapper;
    
    @Override
    public List<Notification> selectPublishedNotifications() {
        return notificationMapper.selectPublishedNotifications();
    }
    
    @Override
    public Notification selectNotificationWithQuestions(Long notificationId) {
        // 查询通知详情
        Notification notification = notificationMapper.selectNotificationById(notificationId);
        
        if (notification != null) {
            // 查询该通知的问题列表
            List<NotificationQuestion> questions = notificationQuestionMapper.selectQuestionsByNotificationId(notificationId);
            // 这里可以将问题列表设置到notification中，如果需要的话可以扩展Notification实体添加questions字段
            // 为了简化，暂时只返回通知基本信息，问题列表可以在Controller层组合返回
        }
        
        return notification;
    }
}
