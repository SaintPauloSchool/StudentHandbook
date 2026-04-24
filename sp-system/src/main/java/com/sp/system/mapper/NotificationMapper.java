package com.sp.system.mapper;

import com.sp.system.entity.Notification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知Mapper接口
 */
public interface NotificationMapper {

    /**
     * 根据ID查询通知详情
     * @param notificationId 通知ID
     * @return 通知详情
     */
    Notification selectNotificationById(@Param("notificationId") Long notificationId);
}
