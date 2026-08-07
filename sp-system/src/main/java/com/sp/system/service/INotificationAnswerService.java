package com.sp.system.service;

import com.sp.system.entity.NotificationAnswer;
import com.sp.system.entity.vo.AnswerItemVO;

/**
 * 通知回答服務
 */
public interface INotificationAnswerService {

    /**
     * 提交家長對通知問題的單條回答
     *
     * @param answerData 單條回答
     * @param userId     當前登入家長的 user_id
     * @param studentId  學籍 student_id
     * @return 成功插入的記錄數
     */
    int submitAnswers(AnswerItemVO answerData, String userId, String studentId);

    /**
     * 查詢學生在指定通知下的回答（取第一條，適用於單問題通知）
     *
     * @param notificationId 通知 ID
     * @param studentId      學籍 student_id
     * @return 回答記錄；無數據時返回 null
     */
    NotificationAnswer getUserAnswer(Long notificationId, String studentId);
}
