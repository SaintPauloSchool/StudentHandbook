package com.sp.common.exception.user;

/**
 * 用戶密碼不正確或不符合規範異常類
 *
 */
public class UserPasswordNotMatchException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserPasswordNotMatchException()
    {
        super("user.password.not.match", null);
    }
}
