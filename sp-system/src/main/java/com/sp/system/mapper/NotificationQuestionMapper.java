package com.sp.system.mapper;

import com.sp.system.entity.NotificationQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知問題Mapper接口
 */
public interface NotificationQuestionMapper {
    
    /**
     * 根據通知ID查詢問題列表
     * @param notificationId 通知ID
     * @return 問題列表
     */
    List<NotificationQuestion> selectQuestionsByNotificationId(@Param("notificationId") Long notificationId);
}
