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
     * 查询用户对该通知的回答
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 答案列表
     */
    List<NotificationAnswer> selectUserAnswers(@Param("notificationId") Long notificationId, @Param("userId") String userId);
    
    /**
     * 检查学生是否已回答该问题
     * @param notificationId 通知ID
     * @param questionId 问题ID
     * @param studentUserId 学生用户ID
     * @return 答案数量
     */
    int checkStudentAnswerExists(@Param("notificationId") Long notificationId, 
                                  @Param("questionId") Long questionId, 
                                  @Param("studentUserId") String studentUserId);

}
