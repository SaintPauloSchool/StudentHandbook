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
     * 根据ID查询通知详情（包含问题列表）
     */
    @Override
    public Map<String, Object> selectNotificationDetail(Long notificationId) {
        // 查询通知详情
        Notification notification = notificationMapper.selectNotificationById(notificationId);

        if (notification == null) {
            return null;
        }
        // 查询问题列表
        List<NotificationQuestion> questions =
                notificationQuestionMapper.selectQuestionsByNotificationId(notificationId);

        Map<String, Object> result = new HashMap<>();
        result.put("notification", notification);
        result.put("questions", questions);

        return result;
    }
}
