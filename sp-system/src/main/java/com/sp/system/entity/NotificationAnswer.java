package com.sp.system.entity;

import java.time.LocalDateTime;

/**
 * 通知回答表實體類
 */
public class NotificationAnswer {
    private Long answerId;            // 答案 ID
    private Long notificationId;      // 通知 ID
    private Long questionId;          // 問題 ID
    private String answerData;        // 答案數據（JSON格式，包含nodeId、nodeTitle、nodeType、answerContent、attachmentUrls）
    private String userId;            // 用戶 ID（parentUserId）
    private String userType;          // 用戶類型（1 學生 2 家長 3 教師）
    private String studentUserId;     // 學生用戶ID（關聯sys_parent_student_relation）
    private LocalDateTime createTime;          // 創建時間

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

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

    public String getAnswerData() {
        return answerData;
    }

    public void setAnswerData(String answerData) {
        this.answerData = answerData;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(String studentUserId) {
        this.studentUserId = studentUserId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
