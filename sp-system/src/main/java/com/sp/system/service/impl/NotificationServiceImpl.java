package com.sp.system.service.impl;

import com.sp.system.entity.Notification;
import com.sp.system.entity.NotificationAnswer;
import com.sp.system.entity.NotificationQuestion;
import com.sp.system.entity.vo.NotificationDetailVO;
import com.sp.system.mapper.NotificationMapper;
import com.sp.system.mapper.NotificationQuestionMapper;
import com.sp.system.service.INotificationAnswerService;
import com.sp.system.service.INotificationService;
import com.sp.system.service.IStudentRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知 Service 實現
 * <p>
 * 負責通知主體與問題列表查詢；帶學生作答狀態的詳情會聯合答案與關係服務組裝。
 */
@Service
public class NotificationServiceImpl implements INotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private NotificationQuestionMapper notificationQuestionMapper;

    @Autowired
    private INotificationAnswerService notificationAnswerService;

    @Autowired
    private IStudentRelationService studentRelationService;

    /**
     * 根據 ID 查詢通知詳情（含問題列表，不含作答狀態）
     *
     * @param notificationId 通知 ID
     * @return 通知詳情；不存在時返回 null
     */
    @Override
    public NotificationDetailVO selectNotificationDetail(Long notificationId) {
        Notification notification = notificationMapper.selectNotificationById(notificationId);
        if (notification == null) {
            return null;
        }

        List<NotificationQuestion> questions =
                notificationQuestionMapper.selectQuestionsByNotificationId(notificationId);

        NotificationDetailVO detail = new NotificationDetailVO();
        detail.setNotification(notification);
        detail.setQuestions(questions);
        return detail;
    }

    /**
     * 查詢通知詳情，並附帶指定學生的作答情況
     *
     * @param notificationId 通知ID
     * @param studentId      學籍 student_id
     * @return 含作答狀態的詳情；通知不存在時返回 null
     */
    @Override
    public NotificationDetailVO selectNotificationDetailForStudent(Long notificationId, String studentId) {
        NotificationDetailVO detail = selectNotificationDetail(notificationId);
        if (detail == null) {
            return null;
        }

        // 查詢該學生是否已提交回答，並組裝作答人展示信息
        NotificationAnswer userAnswer = notificationAnswerService.getUserAnswer(notificationId, studentId);
        if (userAnswer != null) {
            detail.setUserAnswer(userAnswer);
            // 使用答案記錄中的家長 user_id，拼接「學生姓名 - 親屬關係」
            detail.setAnswererInfo(studentRelationService.getAnswererInfo(userAnswer.getUserId(), studentId));
            detail.setHasSubmitted(true);
        } else {
            detail.setUserAnswer(null);
            detail.setAnswererInfo("");
            detail.setHasSubmitted(false);
        }
        return detail;
    }
}
