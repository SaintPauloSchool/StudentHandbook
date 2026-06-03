package com.sp.common.exception.user;

/**
 * 用戶賬號已被刪除
 *
 */
public class UserDeleteException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserDeleteException()
    {
        super("user.password.delete", null);
    }
}
