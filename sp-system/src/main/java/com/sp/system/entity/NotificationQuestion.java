package com.sp.system.entity;

import java.time.LocalDateTime;

/**
 * 通知問題表實體類
 */
public class NotificationQuestion {
    private Long questionId;            // 問題 ID
    private Long notificationId;        // 通知 ID
    private Long parentQuestionId;      // 父問題 ID
    private String questionTitle;       // 問題標題
    private String questionType;        // 問題類型（1 單選 2 多選 3 填空 4 附件上傳 5 邏輯表單）
    private String options;             // 選項列表 (JSON 格式)
    private String isRequired;          // 是否必答（0 否 1 是）
    private Integer sortOrder;          // 排序
    private String logicRules;          // 跳轉邏輯規則 (JSON 格式)
    private String fillBlanks;          // 填空題的填空列表 (JSON 格式)
    private String correctAnswers;      // 填空題的正確答案 (JSON 格式)
    private String content;             // 題目內容
    private LocalDateTime createTime;            // 創建時間

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getParentQuestionId() {
        return parentQuestionId;
    }

    public void setParentQuestionId(Long parentQuestionId) {
        this.parentQuestionId = parentQuestionId;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(String isRequired) {
        this.isRequired = isRequired;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getLogicRules() {
        return logicRules;
    }

    public void setLogicRules(String logicRules) {
        this.logicRules = logicRules;
    }

    public String getFillBlanks() {
        return fillBlanks;
    }

    public void setFillBlanks(String fillBlanks) {
        this.fillBlanks = fillBlanks;
    }

    public String getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(String correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
