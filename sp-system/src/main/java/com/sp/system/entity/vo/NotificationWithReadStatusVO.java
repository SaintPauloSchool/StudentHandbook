package com.sp.system.entity.vo;

import com.sp.system.entity.Notification;

/**
 * 通知列表VO（包含当前用户阅读状态）
 */
public class NotificationWithReadStatusVO extends Notification {

    /** 阅读记录ID（来自 notification_user_read_record） */
    private Long readId;

    /** 是否已读（0未读 1已读；若无对应发送记录则为null） */
    private String isRead;

    /** 发送记录ID */
    private Long sendRecordId;

    public Long getReadId() {
        return readId;
    }

    public void setReadId(Long readId) {
        this.readId = readId;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public Long getSendRecordId() {
        return sendRecordId;
    }

    public void setSendRecordId(Long sendRecordId) {
        this.sendRecordId = sendRecordId;
    }
}
