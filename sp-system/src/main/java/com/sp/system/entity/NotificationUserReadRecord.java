package com.sp.system.entity;

import java.util.Date;

/**
 * 用户通知阅读状态表实体类
 */
public class NotificationUserReadRecord {

    /** 阅读记录ID */
    private Long readId;

    /** 发送记录ID */
    private Long sendRecordId;

    /** 用户ID */
    private String userId;

    /** 用户类型（1学生 2家长 3教师） */
    private String userType;

    /** 是否已读（0未读 1已读） */
    private String isRead;

    /** 阅读时间 */
    private Date readTime;

    /** 回复状态（0未回复 1已回复） */
    private String replyStatus;

    /** 回复时间 */
    private Date replyTime;

    /** 创建时间 */
    private Date createTime;

    public Long getReadId() {
        return readId;
    }

    public void setReadId(Long readId) {
        this.readId = readId;
    }

    public Long getSendRecordId() {
        return sendRecordId;
    }

    public void setSendRecordId(Long sendRecordId) {
        this.sendRecordId = sendRecordId;
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

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    public String getReplyStatus() {
        return replyStatus;
    }

    public void setReplyStatus(String replyStatus) {
        this.replyStatus = replyStatus;
    }

    public Date getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(Date replyTime) {
        this.replyTime = replyTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
