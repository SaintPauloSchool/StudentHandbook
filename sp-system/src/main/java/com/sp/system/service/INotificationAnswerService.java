package com.sp.system.service;

import com.sp.system.entity.NotificationAnswer;

import java.util.List;

/**
 * 通知回答Service接口
 */
public interface INotificationAnswerService {
    
    /**
     * 批量保存答案
     * @param answers 答案列表
     * @return 插入记录数
     */
    int batchSaveAnswers(List<NotificationAnswer> answers);
    
    /**
     * 根据通知ID和用户ID查询答案列表
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 答案列表
     */
    List<NotificationAnswer> selectAnswersByNotificationAndUser(Long notificationId, Long userId);
}
