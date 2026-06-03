package com.sp.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 數據權限過濾註解
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope
{
    /**
     * 部門表的別名
     */
    public String deptAlias() default "";

    /**
     * 用戶表的別名
     */
    public String userAlias() default "";

    /**
     * 權限字符（用於多個角色匹配符合要求的權限）默認根據權限註解@RequiresPermissions獲取，多個權限用逗號分隔開來
     */
    public String permission() default "";
}
