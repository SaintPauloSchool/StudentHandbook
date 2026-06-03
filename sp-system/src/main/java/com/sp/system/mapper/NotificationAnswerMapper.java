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
     * @return 插入記錄數
     */
    int batchInsertAnswers(@Param("answers") List<NotificationAnswer> answers);
    
    /**
     * 查詢用戶對該通知的回答
     * @param notificationId 通知ID
     * @param userId 用戶ID
     * @return 答案列表
     */
    List<NotificationAnswer> selectUserAnswers(@Param("notificationId") Long notificationId, @Param("userId") String userId);
    
    /**
     * 檢查學生是否已回答該問題
     * @param notificationId 通知ID
     * @param questionId 問題ID
     * @param studentUserId 學生用戶ID
     * @return 答案數量
     */
    int checkStudentAnswerExists(@Param("notificationId") Long notificationId, 
                                  @Param("questionId") Long questionId, 
                                  @Param("studentUserId") String studentUserId);

}
