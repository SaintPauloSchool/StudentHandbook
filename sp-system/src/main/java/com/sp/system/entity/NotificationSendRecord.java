package com.sp.system.entity;

import java.time.LocalDateTime;

/**
 * 發送通知記錄主表實體類
 */
public class NotificationSendRecord {

    /** 發送記錄ID */
    private Long sendRecordId;

    /** 通知ID */
    private Long notificationId;

    /** 發送人ID */
    private Long senderId;

    /** 發送人姓名 */
    private String senderName;

    /** 發送時間 */
    private LocalDateTime sendTime;

    /** 發送狀態（0待發送 1發送中 2發送成功 3發送失敗 4部分成功） */
    private String sendStatus;

    /** 應發送總人數 */
    private Integer totalCount;

    /** 發送成功人數 */
    private Integer successCount;

    /** 發送失敗人數 */
    private Integer failCount;

    /** 創建者 */
    private String createBy;

    /** 創建時間 */
    private LocalDateTime createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新時間 */
    private LocalDateTime updateTime;

    /** 備註 */
    private String remark;

    public Long getSendRecordId() {
        return sendRecordId;
    }

    public void setSendRecordId(Long sendRecordId) {
        this.sendRecordId = sendRecordId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
