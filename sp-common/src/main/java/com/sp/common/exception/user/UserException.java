package com.sp.common.exception.user;

import com.sp.common.exception.base.BaseException;

/**
 * 用戶信息異常類
 *
 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args)
    {
        super("user", code, args, null);
    }
}
