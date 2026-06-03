package com.sp.common.constant;

import java.util.Locale;

/**
 * 通用常量信息
 *
 */
public class Constants
{
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * 系統語言
     */
    public static final Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;

    /**
     * http請求
     */
    public static final String HTTP = "http://";

    /**
     * https請求
     */
    public static final String HTTPS = "https://";

    /**
     * 通用成功標識
     */
    public static final String SUCCESS = "0";

    /**
     * 通用失敗標識
     */
    public static final String FAIL = "1";

    /**
     * 登錄成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 註銷
     */
    public static final String LOGOUT = "Logout";

    /**
     * 註冊
     */
    public static final String REGISTER = "Register";

    /**
     * 登錄失敗
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 系統用戶授權緩存
     */
    public static final String SYS_AUTH_CACHE = "sys-authCache";

    /**
     * 參數管理 cache name
     */
    public static final String SYS_CONFIG_CACHE = "sys-config";

    /**
     * 參數管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache name
     */
    public static final String SYS_DICT_CACHE = "sys-dict";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 資源映射路徑 前綴
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * RMI 遠程方法調用
     */
    public static final String LOOKUP_RMI = "rmi:";

    /**
     * LDAP 遠程方法調用
     */
    public static final String LOOKUP_LDAP = "ldap:";

    /**
     * LDAPS 遠程方法調用
     */
    public static final String LOOKUP_LDAPS = "ldaps:";

    /**
     * 定時任務白名單配置（僅允許訪問的包名，如其他需要可以自行添加）
     */
    public static final String[] JOB_WHITELIST_STR = { "com.sp.quartz.task" };

    /**
     * 定時任務違規的字符
     */
    public static final String[] JOB_ERROR_STR = { "java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "org.springframework", "org.apache", "com.sp.common.utils.file", "com.sp.common.config", "com.sp.generator" };
}