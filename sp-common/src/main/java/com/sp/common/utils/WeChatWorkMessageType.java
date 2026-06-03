package com.sp.common.utils;

/**
 * 企業微信消息類型枚舉
 */
public enum WeChatWorkMessageType {
    
    /**
     * 文本消息
     */
    TEXT("text"),
    
    /**
     * 圖片消息
     */
    IMAGE("image"),
    
    /**
     * 語音消息
     */
    VOICE("voice"),
    
    /**
     * 視頻消息
     */
    VIDEO("video"),
    
    /**
     * 文件消息
     */
    FILE("file"),
    
    /**
     * 文本卡片消息
     */
    TEXTCARD("textcard"),
    
    /**
     * 圖文消息
     */
    NEWS("news"),
    
    /**
     * 圖文消息（mpnews）
     */
    MPNEWS("mpnews"),
    
    /**
     * markdown消息
     */
    MARKDOWN("markdown"),
    
    /**
     * 小程序通知消息
     */
    MINIPROGRAM_NOTICE("miniprogram_notice");

    private final String value;

    WeChatWorkMessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}