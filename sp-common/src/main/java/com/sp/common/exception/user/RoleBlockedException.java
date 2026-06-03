package com.sp.common.exception.user;

/**
 * 角色鎖定異常類
 *
 */
public class RoleBlockedException extends UserException
{
    private static final long serialVersionUID = 1L;

    public RoleBlockedException()
    {
        super("role.blocked", null);
    }
}
