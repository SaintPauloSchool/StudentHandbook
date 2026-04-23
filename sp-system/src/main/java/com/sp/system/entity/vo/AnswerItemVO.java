package com.sp.system.entity.vo;

/**
 * 答案项VO
 */
public class AnswerItemVO {
    
    /**
     * 通知ID
     */
    private Long notificationId;
    
    /**
     * 问题ID
     */
    private Long questionId;
    
    /**
     * 答案数据（JSON数组格式）
     */
    private Object answerData;

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Object getAnswerData() {
        return answerData;
    }

    public void setAnswerData(Object answerData) {
        this.answerData = answerData;
    }
}
