package com.sp.system.mapper;

import com.sp.system.entity.NotificationAnswer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知回答Mapper接口
 */
public interface NotificationAnswerMapper {
    
    /**
     * 批量插入答案
     * @param answers 答案列表
     * @return 插入记录数
     */
    int batchInsertAnswers(@Param("answers") List<NotificationAnswer> answers);
    
    /**
     * 根据通知ID和用户ID查询答案列表
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 答案列表
     */
    List<NotificationAnswer> selectAnswersByNotificationAndUser(
        @Param("notificationId") Long notificationId, 
        @Param("userId") Long userId
    );
}
