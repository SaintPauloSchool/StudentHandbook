package com.sp.system.entity;

import java.time.LocalDateTime;

/**
 * 通知主表实体类
 */
public class Notification {
    private Long notificationId;        // 通知 ID
    private String title;               // 通知标题
    private String content;             // 通知正文
    private Long senderId;              // 发送人 ID
    private String senderName;          // 发送人姓名
    private String jumpUrl;             // 跳转链接
    private String attachmentUrls;      // 附件/图片 URL 列表 (JSON 格式)
    private String status;              // 状态（0 草稿 1 已发布 2 已撤回）
    private LocalDateTime replyDeadline;         // 回复截止时间
    private String createBy;            // 创建者
    private LocalDateTime createTime;            // 创建时间
    private String updateBy;            // 更新者
    private LocalDateTime updateTime;            // 更新时间
    private String remark;              // 备注

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getJumpUrl() {
        return jumpUrl;
    }

    public void setJumpUrl(String jumpUrl) {
        this.jumpUrl = jumpUrl;
    }

    public String getAttachmentUrls() {
        return attachmentUrls;
    }

    public void setAttachmentUrls(String attachmentUrls) {
        this.attachmentUrls = attachmentUrls;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getReplyDeadline() {
        return replyDeadline;
    }

    public void setReplyDeadline(LocalDateTime replyDeadline) {
        this.replyDeadline = replyDeadline;
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
