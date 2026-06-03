package com.sp.framework.aspectj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import com.sp.common.core.context.PermissionContextHolder;
import com.sp.common.utils.StringUtils;

/**
 * 自定義權限攔截器，將權限字符串放到當前請求中以便用於多個角色匹配符合要求的權限
 *
 */
@Aspect
@Component
public class PermissionsAspect
{
    @Before("@annotation(controllerPreAuthorize)")
    public void doBefore(JoinPoint point, PreAuthorize controllerPreAuthorize) throws Throwable
    {
        handleRequiresPermissions(point, controllerPreAuthorize);
    }

    protected void handleRequiresPermissions(final JoinPoint joinPoint, PreAuthorize preAuthorize)
    {
        // 提取權限表達式中的權限字符串
        String permissionExpression = preAuthorize.value();
        // 簡單提取權限名稱（假設格式爲"hasAuthority('permission')"）
        if (permissionExpression.startsWith("hasAuthority('") && permissionExpression.endsWith("')")) {
            String permission = permissionExpression.substring(14, permissionExpression.length() - 2);
            PermissionContextHolder.setContext(permission);
        }
    }
}