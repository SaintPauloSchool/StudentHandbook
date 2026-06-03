package com.sp.system.service;

import com.sp.system.entity.NotificationAnswer;
import com.sp.system.entity.vo.AnswerItemVO;

import java.util.List;

/**
 * 通知回答Service接口
 */
public interface INotificationAnswerService {
    
    /**
     * 將前端傳來的答案數據轉換爲實體對象並保存
     * @param answerData 前端傳來的答案數據（單個問題）
     * @param userId 用戶ID（parentUserId）
     * @param userType 用戶類型
     * @param studentUserId 學生用戶ID
     * @return 插入記錄數
     * @throws RuntimeException 如果學生已經回答過該問題
     */
    int submitAnswers(AnswerItemVO answerData, String userId, String userType, String studentUserId);
    
    /**
     * 根據家長ID獲取對應的學生ID
     * @param parentUserId 家長用戶ID
     * @return 學生用戶ID（如果有多個學生，返回第一個）
     */
    String getStudentUserIdByParentId(String parentUserId);
    
    /**
     * 檢查學生是否已回答該通知的問題
     * @param notificationId 通知ID
     * @param questionId 問題ID
     * @param studentUserId 學生用戶ID
     * @return true-已回答，false-未回答
     */
    boolean checkStudentAnswerExists(Long notificationId, Long questionId, String studentUserId);
    
    /**
     * 查詢用戶對該通知的回答（只有一條記錄）
     * @param notificationId 通知ID
     * @param studentUserId 學生用戶ID
     * @return 答案對象（只有一條記錄）
     */
    NotificationAnswer getUserAnswer(Long notificationId, String studentUserId);
}
