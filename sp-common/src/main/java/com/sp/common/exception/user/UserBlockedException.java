package com.sp.common.exception.user;

/**
 * 用戶鎖定異常類
 *
 */
public class UserBlockedException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserBlockedException()
    {
        super("user.blocked", null);
    }
}
