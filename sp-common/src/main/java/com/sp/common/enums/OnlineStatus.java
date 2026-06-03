package com.sp.common.enums;

/**
 * 用戶會話
 *
 */
public enum OnlineStatus
{
    /** 用戶狀態 */
    on_line("在線"), off_line("離線");

    private final String info;

    private OnlineStatus(String info)
    {
        this.info = info;
    }

    public String getInfo()
    {
        return info;
    }
}
