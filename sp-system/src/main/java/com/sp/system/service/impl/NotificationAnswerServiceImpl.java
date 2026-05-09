package com.sp.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.sp.system.entity.NotificationAnswer;
import com.sp.system.entity.ParentStudentRelation;
import com.sp.system.entity.vo.AnswerItemVO;
import com.sp.common.exception.DuplicateSubmissionException;
import com.sp.system.mapper.NotificationAnswerMapper;
import com.sp.system.mapper.ParentStudentRelationMapper;
import com.sp.system.service.INotificationAnswerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知回答Service实现类
 */
@Service
public class NotificationAnswerServiceImpl implements INotificationAnswerService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationAnswerServiceImpl.class);
    
    @Autowired
    private NotificationAnswerMapper notificationAnswerMapper;
    
    @Autowired
    private ParentStudentRelationMapper parentStudentRelationMapper;
    
    /**
     * 将前端传来的答案数据转换为实体对象并保存
     * @param answerData 前端传来的答案数据（单个问题）
     * @param userId 用户ID（parentUserId）
     * @param userType 用户类型
     * @param studentUserId 学生用户ID
     * @return 插入记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitAnswers(AnswerItemVO answerData, String userId, String userType, String studentUserId) {
        if (answerData == null) {
            logger.warn("答案数据为空，无需保存");
            return 0;
        }

        try {
            // 检查学生是否已经回答过这个问题
            boolean exists = checkStudentAnswerExists(
                answerData.getNotificationId(), 
                answerData.getQuestionId(), 
                studentUserId
            );
            if (exists) {
                logger.warn("学生 {} 已回答过通知 {} 的问题 {}", 
                    studentUserId, answerData.getNotificationId(), answerData.getQuestionId());
                throw new DuplicateSubmissionException("家长已回答过此问题，请勿重复提交");
            }
            
            // 转换答案数据
            NotificationAnswer answer = convertToNotificationAnswer(answerData, userId, userType, studentUserId);
            
            // 保存
            List<NotificationAnswer> answers = new java.util.ArrayList<>();
            answers.add(answer);
            int result = notificationAnswerMapper.batchInsertAnswers(answers);
            logger.info("成功提交答案记录，学生ID: {}", studentUserId);
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("提交答案失败: {}", e.getMessage(), e);
            throw new RuntimeException("提交答案失败: " + e.getMessage());
        }
    }
    
    /**
     * 将前端传来的答案数据转换为实体对象
     * @param answerData 前端答案数据（单个问题）
     * @param userId 用户ID（parentUserId）
     * @param userType 用户类型
     * @param studentUserId 学生用户ID
     * @return 答案实体对象
     */
    private NotificationAnswer convertToNotificationAnswer(AnswerItemVO answerData, String userId, String userType, String studentUserId) {
        NotificationAnswer answer = new NotificationAnswer();
        
        // 设置通知ID
        answer.setNotificationId(answerData.getNotificationId());
        
        // 设置问题ID
        answer.setQuestionId(answerData.getQuestionId());
        
        // 设置答案数据（JSON格式）
        Object answerDataObj = answerData.getAnswerData();
        if (answerDataObj instanceof List) {
            // 如果是数组，转换为JSON字符串
            answer.setAnswerData(JSON.toJSONString(answerDataObj));
        } else if (answerDataObj instanceof String) {
            // 如果已经是字符串，直接使用
            answer.setAnswerData((String) answerDataObj);
        }
        
        // 设置用户ID和类型
        answer.setUserId(userId);
        answer.setUserType(userType);
        
        // 设置学生用户ID
        answer.setStudentUserId(studentUserId);
        
        return answer;
    }
    
    /**
     * 根据家长ID获取对应的学生ID
     * @param parentUserId 家长用户ID
     * @return 学生用户ID（如果有多个学生，返回第一个）
     */
    @Override
    public String getStudentUserIdByParentId(String parentUserId) {
        try {
            List<ParentStudentRelation> relations = parentStudentRelationMapper.selectByParentId(parentUserId);
            if (relations == null || relations.isEmpty()) {
                logger.warn("家长 {} 未绑定任何学生", parentUserId);
                return null;
            }
            // 返回第一个学生的ID
            return relations.get(0).getStudentUserId();
        } catch (Exception e) {
            logger.error("查询家长学生关系失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 检查学生是否已回答该通知的问题
     * @param notificationId 通知ID
     * @param questionId 问题ID
     * @param studentUserId 学生用户ID
     * @return true-已回答，false-未回答
     */
    @Override
    public boolean checkStudentAnswerExists(Long notificationId, Long questionId, String studentUserId) {
        try {
            int count = notificationAnswerMapper.checkStudentAnswerExists(notificationId, questionId, studentUserId);
            return count > 0;
        } catch (Exception e) {
            logger.error("检查学生答案失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 查询用户对该通知的回答（只有一条记录）
     * @param notificationId 通知ID
     * @param studentUserId 学生用户ID
     * @return 答案对象（只有一条记录）
     */
    @Override
    public NotificationAnswer getUserAnswer(Long notificationId, String studentUserId) {
        try {
            List<NotificationAnswer> answers = notificationAnswerMapper.selectUserAnswers(notificationId, studentUserId);
            // 只返回第一条记录
            return (answers != null && !answers.isEmpty()) ? answers.get(0) : null;
        } catch (Exception e) {
            logger.error("查询用户答案失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询用户答案失败: " + e.getMessage());
        }
    }
}
