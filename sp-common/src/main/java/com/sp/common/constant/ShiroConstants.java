package com.sp.common.constant;

/**
 * Shiro通用常量
 *
 */
public class ShiroConstants
{
    /**
     * 當前登錄的用戶
     */
    public static final String CURRENT_USER = "currentUser";

    /**
     * 用戶名字段
     */
    public static final String CURRENT_USERNAME = "username";

    /**
     * 鎖定屏幕字段
     */
    public static final String LOCK_SCREEN = "lockscreen";

    /**
     * 消息key
     */
    public static final String MESSAGE = "message";

    /**
     * 錯誤key
     */
    public static final String ERROR = "errorMsg";

    /**
     * csrf session content
     */
    public static final String CSRF_TOKEN = "csrf_token";

    /**
     * csrf request header
     */
    public static final String X_CSRF_TOKEN = "X-CSRF-Token";

    /**
     * 當前在線會話
     */
    public static final String ONLINE_SESSION = "online_session";

    /**
     * 驗證碼key
     */
    public static final String CURRENT_CAPTCHA = "captcha";

    /**
     * 驗證碼開關
     */
    public static final String CURRENT_ENABLED = "captchaEnabled";

    /**
     * 驗證碼類型
     */
    public static final String CURRENT_TYPE = "captchaType";

    /**
     * 驗證碼
     */
    public static final String CURRENT_VALIDATECODE = "validateCode";

    /**
     * 驗證碼錯誤
     */
    public static final String CAPTCHA_ERROR = "captchaError";

    /**
     * 登錄記錄緩存
     */
    public static final String LOGIN_RECORD_CACHE = "loginRecordCache";

    /**
     * 系統活躍用戶緩存
     */
    public static final String SYS_USERCACHE = "sys-userCache";
}
