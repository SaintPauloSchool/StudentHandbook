package com.sp.system.mapper;

import com.sp.system.entity.Notification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知Mapper接口
 */
public interface NotificationMapper {
    
    /**
     * 查询已发布的通知列表
     * @return 通知列表
     */
    List<Notification> selectPublishedNotifications();
    
    /**
     * 根据ID查询通知详情
     * @param notificationId 通知ID
     * @return 通知详情
     */
    Notification selectNotificationById(@Param("notificationId") Long notificationId);
}
