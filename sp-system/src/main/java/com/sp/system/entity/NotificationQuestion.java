package com.sp.system.entity;

import java.time.LocalDateTime;

/**
 * 通知问题表实体类
 */
public class NotificationQuestion {
    private Long questionId;            // 问题 ID
    private Long notificationId;        // 通知 ID
    private Long parentQuestionId;      // 父问题 ID
    private String questionTitle;       // 问题标题
    private String questionType;        // 问题类型（1 单选 2 多选 3 填空 4 附件上传 5 逻辑表单）
    private String options;             // 选项列表 (JSON 格式)
    private String isRequired;          // 是否必答（0 否 1 是）
    private Integer sortOrder;          // 排序
    private String logicRules;          // 跳转逻辑规则 (JSON 格式)
    private String fillBlanks;          // 填空题的填空列表 (JSON 格式)
    private String correctAnswers;      // 填空题的正确答案 (JSON 格式)
    private String content;             // 题目内容
    private LocalDateTime createTime;            // 创建时间

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
