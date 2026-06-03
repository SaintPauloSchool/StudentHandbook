package com.sp.system.entity;

import java.time.LocalDateTime;

/**
 * 用戶通知閱讀狀態表實體類
 */
public class NotificationUserReadRecord {

    /** 閱讀記錄ID */
    private Long readId;

    /** 發送記錄ID */
    private Long sendRecordId;

    /** 用戶ID */
    private String userId;

    /** 用戶類型（1學生 2家長 3教師） */
    private String userType;

    /** 是否已讀（0未讀 1已讀） */
    private String isRead;

    /** 閱讀時間 */
    private LocalDateTime readTime;

    /** 回復狀態（0未回復 1已回復） */
    private String replyStatus;

    /** 回復時間 */
    private LocalDateTime replyTime;

    /** 企業微信發送狀態（0發送失敗 1發送成功） */
    private String sendStatus;

    /** 創建時間 */
    private LocalDateTime createTime;

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

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public String getReplyStatus() {
        return replyStatus;
    }

    public void setReplyStatus(String replyStatus) {
        this.replyStatus = replyStatus;
    }

    public LocalDateTime getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(LocalDateTime replyTime) {
        this.replyTime = replyTime;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
