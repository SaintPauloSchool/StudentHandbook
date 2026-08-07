package com.sp.system.entity.vo;

import com.sp.system.entity.Notification;
import com.sp.system.entity.NotificationAnswer;
import com.sp.system.entity.NotificationQuestion;

import java.util.List;

/**
 * 通知詳情響應 VO
 */
public class NotificationDetailVO {

    private Notification notification;

    private List<NotificationQuestion> questions;

    private NotificationAnswer userAnswer;

    private String answererInfo;

    private boolean hasSubmitted;

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public List<NotificationQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<NotificationQuestion> questions) {
        this.questions = questions;
    }

    public NotificationAnswer getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(NotificationAnswer userAnswer) {
        this.userAnswer = userAnswer;
    }

    public String getAnswererInfo() {
        return answererInfo;
    }

    public void setAnswererInfo(String answererInfo) {
        this.answererInfo = answererInfo;
    }

    public boolean isHasSubmitted() {
        return hasSubmitted;
    }

    public void setHasSubmitted(boolean hasSubmitted) {
        this.hasSubmitted = hasSubmitted;
    }
}
