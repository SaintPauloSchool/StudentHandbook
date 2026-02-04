package com.sp.framework.aspectj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import com.sp.common.core.context.PermissionContextHolder;
import com.sp.common.utils.StringUtils;

/**
 * 自定义权限拦截器，将权限字符串放到当前请求中以便用于多个角色匹配符合要求的权限
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
        // 提取权限表达式中的权限字符串
        String permissionExpression = preAuthorize.value();
        // 简单提取权限名称（假设格式为"hasAuthority('permission')"）
        if (permissionExpression.startsWith("hasAuthority('") && permissionExpression.endsWith("')")) {
            String permission = permissionExpression.substring(14, permissionExpression.length() - 2);
            PermissionContextHolder.setContext(permission);
        }
    }
}