package com.sp.common.exception.user;

/**
 * 黑名單IP異常類
 *
 */
public class BlackListException extends UserException
{
    private static final long serialVersionUID = 1L;

    public BlackListException()
    {
        super("login.blocked", null);
    }
}
