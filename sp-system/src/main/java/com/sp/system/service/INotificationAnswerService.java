package com.sp.system.service;

import com.sp.system.entity.NotificationAnswer;
import com.sp.system.entity.vo.AnswerItemVO;

import java.util.List;

/**
 * 通知回答Service接口
 */
public interface INotificationAnswerService {
    
    /**
     * 将前端传来的答案数据转换为实体对象并保存
     * @param answerData 前端传来的答案数据（单个问题）
     * @param userId 用户ID（parentUserId）
     * @param userType 用户类型
     * @return 插入记录数
     * @throws RuntimeException 如果学生已经回答过该问题
     */
    int submitAnswers(AnswerItemVO answerData, String userId, String userType);
    
    /**
     * 根据家长ID获取对应的学生ID
     * @param parentUserId 家长用户ID
     * @return 学生用户ID（如果有多个学生，返回第一个）
     */
    String getStudentUserIdByParentId(String parentUserId);
    
    /**
     * 检查学生是否已回答该通知的问题
     * @param notificationId 通知ID
     * @param questionId 问题ID
     * @param studentUserId 学生用户ID
     * @return true-已回答，false-未回答
     */
    boolean checkStudentAnswerExists(Long notificationId, Long questionId, String studentUserId);
    
    /**
     * 查询用户对该通知的回答
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 答案列表
     */
    List<NotificationAnswer> getUserAnswers(Long notificationId, String userId);
}
