package com.sp.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.sp.common.exception.DuplicateSubmissionException;
import com.sp.system.entity.NotificationAnswer;
import com.sp.system.entity.vo.AnswerItemVO;
import com.sp.system.mapper.NotificationAnswerMapper;
import com.sp.system.service.INotificationAnswerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 通知回答服務實現
 * <p>
 * 負責家長代學生提交通知回答，並在插入前校驗是否重複提交。
 */
@Service
public class NotificationAnswerServiceImpl implements INotificationAnswerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationAnswerServiceImpl.class);

    @Autowired
    private NotificationAnswerMapper notificationAnswerMapper;

    /**
     * 提交家長對通知問題的單條回答。
     *
     * @param answerData 單條回答（通知 ID、問題 ID、答案內容）
     * @param userId     當前登入家長的 user_id
     * @param studentId  當前選中學生的 student_id（業務主鍵，非 student_user_id）
     * @return 成功插入的記錄數，通常為 1；answerData 為 null 時返回 0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitAnswers(AnswerItemVO answerData, String userId, String studentId) {
        if (answerData == null) {
            return 0;
        }

        Long notificationId = answerData.getNotificationId();
        Long questionId = answerData.getQuestionId();

        // 插入前先查詢，避免重複提交
        if (notificationAnswerMapper.countByNotificationQuestionStudent(notificationId, questionId, studentId) > 0) {
            throw new DuplicateSubmissionException("家長已回答過此問題，請勿重複提交");
        }

        NotificationAnswer answer = new NotificationAnswer();
        answer.setNotificationId(notificationId);
        answer.setQuestionId(questionId);

        // 答案可能是多選列表或純文字，統一序列化後存入 answer_data
        Object answerDataObj = answerData.getAnswerData();
        if (answerDataObj instanceof List) {
            answer.setAnswerData(JSON.toJSONString(answerDataObj));
        } else if (answerDataObj instanceof String) {
            answer.setAnswerData((String) answerDataObj);
        }

        answer.setUserId(userId);
        answer.setStudentId(studentId);
        answer.setCreateTime(LocalDateTime.now());

        int result = notificationAnswerMapper.batchInsertAnswers(Collections.singletonList(answer));

        logger.info("成功提交答案記錄，學生ID: {}", studentId);
        return result;
    }

    /**
     * 查詢學生在指定通知下的回答。
     * <p>
     * 當前業務每個通知僅展示一條回答，故取列表第一條；多問題場景可改為按 questionId 篩選。
     *
     * @param notificationId 通知 ID
     * @param studentId      學籍 student_id
     * @return 回答記錄；無數據時返回 null
     */
    @Override
    public NotificationAnswer getUserAnswer(Long notificationId, String studentId) {
        List<NotificationAnswer> answers = notificationAnswerMapper.selectUserAnswers(notificationId, studentId);
        return (answers != null && !answers.isEmpty()) ? answers.get(0) : null;
    }
}
