package com.sp.system.entity;

import java.util.Date;

/**
 * 通知回答表实体类
 */
public class NotificationAnswer {
    private Long answerId;            // 答案 ID
    private Long notificationId;      // 通知 ID
    private Long questionId;          // 问题 ID
    private String nodeId;            // 节点 ID（逻辑表单使用）
    private String nodeTitle;         // 节点标题（逻辑表单使用）
    private String nodeType;          // 节点类型（逻辑表单使用）
    private Long userId;              // 用户 ID
    private String userType;          // 用户类型（1 学生 2 家长 3 教师）
    private String answerContent;     // 答案内容
    private String attachmentUrls;    // 附件 URL 列表 (JSON 格式)
    private Date createTime;          // 创建时间

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

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeTitle() {
        return nodeTitle;
    }

    public void setNodeTitle(String nodeTitle) {
        this.nodeTitle = nodeTitle;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getAnswerContent() {
        return answerContent;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }

    public String getAttachmentUrls() {
        return attachmentUrls;
    }

    public void setAttachmentUrls(String attachmentUrls) {
        this.attachmentUrls = attachmentUrls;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
