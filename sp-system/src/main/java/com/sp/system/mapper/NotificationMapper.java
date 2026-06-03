package com.sp.system.mapper;

import com.sp.system.entity.Notification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知Mapper接口
 */
public interface NotificationMapper {

    /**
     * 根據ID查詢通知詳情
     * @param notificationId 通知ID
     * @return 通知詳情
     */
    Notification selectNotificationById(@Param("notificationId") Long notificationId);
}
