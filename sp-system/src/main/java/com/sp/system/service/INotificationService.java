package com.sp.system.service;

import com.sp.system.entity.Notification;

import java.util.List;
import java.util.Map;

/**
 * 通知Service接口
 */
public interface INotificationService {

    /**
     * 分页查询已发布的通知列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 通知列表
     */
    List<Notification> selectPublishedNotificationsPage(int pageNum, int pageSize);
    
    /**
     * 查询已发布的通知总数
     * @return 总数
     */
    int countPublishedNotifications();

    /**
     * 根据ID查询通知详情（包含问题列表）
     * @param notificationId 通知ID
     * @return 包含通知和问题列表的Map
     */
    Map<String, Object> selectNotificationDetail(Long notificationId);
}
