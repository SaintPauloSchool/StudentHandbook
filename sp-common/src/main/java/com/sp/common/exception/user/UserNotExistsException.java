package com.sp.common.exception.user;

/**
 * 用戶不存在異常類
 *
 */
public class UserNotExistsException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserNotExistsException()
    {
        super("user.not.exists", null);
    }
}
