package com.sp.system.mapper;

import com.sp.system.entity.NotificationQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知问题Mapper接口
 */
public interface NotificationQuestionMapper {
    
    /**
     * 根据通知ID查询问题列表
     * @param notificationId 通知ID
     * @return 问题列表
     */
    List<NotificationQuestion> selectQuestionsByNotificationId(@Param("notificationId") Long notificationId);
}
