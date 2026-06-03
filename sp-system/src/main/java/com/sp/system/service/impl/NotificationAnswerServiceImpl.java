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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通知回答Service實現類
 */
@Service
public class NotificationAnswerServiceImpl implements INotificationAnswerService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationAnswerServiceImpl.class);
    
    @Autowired
    private NotificationAnswerMapper notificationAnswerMapper;
    
    @Autowired
    private ParentStudentRelationMapper parentStudentRelationMapper;
    
    /**
     * 將前端傳來的答案數據轉換爲實體對象並保存
     * @param answerData 前端傳來的答案數據（單個問題）
     * @param userId 用戶ID（parentUserId）
     * @param userType 用戶類型
     * @param studentUserId 學生用戶ID
     * @return 插入記錄數
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitAnswers(AnswerItemVO answerData, String userId, String userType, String studentUserId) {
        if (answerData == null) {
            logger.warn("答案數據爲空，無需保存");
            return 0;
        }

        try {
            // 轉換答案數據
            NotificationAnswer answer = convertToNotificationAnswer(answerData, userId, userType, studentUserId);

            List<NotificationAnswer> answers = new ArrayList<>();
            answers.add(answer);

            // INSERT IGNORE：若 DB 唯一約束衝突（並發重複提交），受影響行數為 0
            // 避免先 SELECT 再 INSERT 的競態條件（TOCTOU race condition）
            int result = notificationAnswerMapper.batchInsertAnswers(answers);

            if (result == 0) {
                logger.warn("學生 {} 已回答過通知 {} 的問題 {}（並發重複提交被忽略）",
                    studentUserId, answerData.getNotificationId(), answerData.getQuestionId());
                throw new DuplicateSubmissionException("家長已回答過此問題，請勿重複提交");
            }

            logger.info("成功提交答案記錄，學生ID: {}", studentUserId);
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("提交答案失敗: {}", e.getMessage(), e);
            throw new RuntimeException("提交答案失敗: " + e.getMessage());
        }
    }

    
    /**
     * 將前端傳來的答案數據轉換爲實體對象
     * @param answerData 前端答案數據（單個問題）
     * @param userId 用戶ID（parentUserId）
     * @param userType 用戶類型
     * @param studentUserId 學生用戶ID
     * @return 答案實體對象
     */
    private NotificationAnswer convertToNotificationAnswer(AnswerItemVO answerData, String userId, String userType, String studentUserId) {
        NotificationAnswer answer = new NotificationAnswer();
        
        // 設置通知ID
        answer.setNotificationId(answerData.getNotificationId());
        
        // 設置問題ID
        answer.setQuestionId(answerData.getQuestionId());
        
        // 設置答案數據（JSON格式）
        Object answerDataObj = answerData.getAnswerData();
        if (answerDataObj instanceof List) {
            // 如果是數組，轉換爲JSON字符串
            answer.setAnswerData(JSON.toJSONString(answerDataObj));
        } else if (answerDataObj instanceof String) {
            // 如果已經是字符串，直接使用
            answer.setAnswerData((String) answerDataObj);
        }
        
        // 設置用戶ID和類型
        answer.setUserId(userId);
        answer.setUserType(userType);
        
        // 設置學生用戶ID
        answer.setStudentUserId(studentUserId);
        
        // 設置創建時間
        answer.setCreateTime(LocalDateTime.now());
        
        return answer;
    }
    
    /**
     * 根據家長ID獲取對應的學生ID
     * @param parentUserId 家長用戶ID
     * @return 學生用戶ID（如果有多個學生，返回第一個）
     */
    @Override
    public String getStudentUserIdByParentId(String parentUserId) {
        try {
            List<ParentStudentRelation> relations = parentStudentRelationMapper.selectByParentId(parentUserId);
            if (relations == null || relations.isEmpty()) {
                logger.warn("家長 {} 未綁定任何學生", parentUserId);
                return null;
            }
            // 返回第一個學生的ID
            return relations.get(0).getStudentUserId();
        } catch (Exception e) {
            logger.error("查詢家長學生關係失敗: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 檢查學生是否已回答該通知的問題
     * @param notificationId 通知ID
     * @param questionId 問題ID
     * @param studentUserId 學生用戶ID
     * @return true-已回答，false-未回答
     */
    @Override
    public boolean checkStudentAnswerExists(Long notificationId, Long questionId, String studentUserId) {
        try {
            int count = notificationAnswerMapper.checkStudentAnswerExists(notificationId, questionId, studentUserId);
            return count > 0;
        } catch (Exception e) {
            logger.error("檢查學生答案失敗: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 查詢用戶對該通知的回答（只有一條記錄）
     * @param notificationId 通知ID
     * @param studentUserId 學生用戶ID
     * @return 答案對象（只有一條記錄）
     */
    @Override
    public NotificationAnswer getUserAnswer(Long notificationId, String studentUserId) {
        try {
            List<NotificationAnswer> answers = notificationAnswerMapper.selectUserAnswers(notificationId, studentUserId);
            // 只返回第一條記錄
            return (answers != null && !answers.isEmpty()) ? answers.get(0) : null;
        } catch (Exception e) {
            logger.error("查詢用戶答案失敗: {}", e.getMessage(), e);
            throw new RuntimeException("查詢用戶答案失敗: " + e.getMessage());
        }
    }
}
