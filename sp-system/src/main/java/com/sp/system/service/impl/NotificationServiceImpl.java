package com.sp.system.service.impl;

import com.sp.system.entity.Notification;
import com.sp.system.entity.NotificationQuestion;
import com.sp.system.mapper.NotificationMapper;
import com.sp.system.mapper.NotificationQuestionMapper;
import com.sp.system.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知Service实现类
 */
@Service
public class NotificationServiceImpl implements INotificationService {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private NotificationQuestionMapper notificationQuestionMapper;

    /**
     * 分页查询已发布通知列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return
     */
    @Override
    public List<Notification> selectPublishedNotificationsPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return notificationMapper.selectPublishedNotificationsPage(offset, pageSize);
    }

    /**
     * 查询已发布通知总数
     * @return
     */
    @Override
    public int countPublishedNotifications() {
        return notificationMapper.countPublishedNotifications();
    }

    /**
     * 根据ID查询通知详情（包含问题列表）
     * @param notificationId
     * @return
     */
    @Override
    public Map<String, Object> selectNotificationDetail(Long notificationId) {
        // 查询通知详情
        Notification notification = notificationMapper.selectNotificationById(notificationId);
        
        if (notification == null) {
            return null;
        }
        
        // 查询该通知的问题列表
        List<NotificationQuestion> questions = notificationQuestionMapper.selectQuestionsByNotificationId(notificationId);
        
        // 组合返回通知和问题列表
        Map<String, Object> result = new HashMap<>();
        result.put("notification", notification);
        result.put("questions", questions);
        
        return result;
    }
}
