package com.sp.system.entity.vo;

import com.sp.system.entity.Notification;

/**
 * 通知列表VO（包含當前用戶閱讀狀態）
 */
public class NotificationWithReadStatusVO extends Notification {

    /** 閱讀記錄ID（來自 notification_user_read_record） */
    private Long readId;

    /** 是否已讀（0未讀 1已讀；若無對應發送記錄則爲null） */
    private String isRead;

    /** 發送記錄ID */
    private Long sendRecordId;

    /** 企業微信發送狀態（0發送失敗 1發送成功） */
    private String sendStatus;

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

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }
}
