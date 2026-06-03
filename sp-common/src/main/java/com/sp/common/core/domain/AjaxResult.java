package com.sp.common.core.domain;

import java.util.HashMap;
import java.util.Objects;
import com.sp.common.utils.StringUtils;

/**
 * 操作消息提醒
 *
 */
public class


AjaxResult extends HashMap<String, Object>
{
    private static final long serialVersionUID = 1L;

    /** 狀態碼 */
    public static final String CODE_TAG = "code";

    /** 返回內容 */
    public static final String MSG_TAG = "msg";

    /** 數據對象 */
    public static final String DATA_TAG = "data";

    /**
     * 狀態類型
     */
    public enum Type
    {
        /** 成功 */
        SUCCESS(200),
        /** 警告 */
        WARN(301),
        /** 錯誤 */
        ERROR(500);
        private final int value;

        Type(int value)
        {
            this.value = value;
        }

        public int value()
        {
            return this.value;
        }
    }

    /**
     * 初始化一個新創建的 AjaxResult 對象，使其表示一個空消息。
     */
    public AjaxResult()
    {
    }

    /**
     * 初始化一個新創建的 AjaxResult 對象
     *
     * @param type 狀態類型
     * @param msg 返回內容
     */
    public AjaxResult(Type type, String msg)
    {
        super.put(CODE_TAG, type.value);
        super.put(MSG_TAG, msg);
    }

    /**
     * 初始化一個新創建的 AjaxResult 對象
     *
     * @param type 狀態類型
     * @param msg 返回內容
     * @param data 數據對象
     */
    public AjaxResult(Type type, String msg, Object data)
    {
        super.put(CODE_TAG, type.value);
        super.put(MSG_TAG, msg);
        if (StringUtils.isNotNull(data))
        {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static AjaxResult success()
    {
        return AjaxResult.success("操作成功");
    }

    /**
     * 返回成功數據
     *
     * @return 成功消息
     */
    public static AjaxResult success(Object data)
    {
        return AjaxResult.success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回內容
     * @return 成功消息
     */
    public static AjaxResult success(String msg)
    {
        return AjaxResult.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回內容
     * @param data 數據對象
     * @return 成功消息
     */
    public static AjaxResult success(String msg, Object data)
    {
        return new AjaxResult(Type.SUCCESS, msg, data);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回內容
     * @return 警告消息
     */
    public static AjaxResult warn(String msg)
    {
        return AjaxResult.warn(msg, null);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回內容
     * @param data 數據對象
     * @return 警告消息
     */
    public static AjaxResult warn(String msg, Object data)
    {
        return new AjaxResult(Type.WARN, msg, data);
    }

    /**
     * 返回錯誤消息
     *
     * @return
     */
    public static AjaxResult error()
    {
        return AjaxResult.error("操作失敗");
    }

    /**
     * 返回錯誤消息
     *
     * @param msg 返回內容
     * @return 警告消息
     */
    public static AjaxResult error(String msg)
    {
        return AjaxResult.error(msg, null);
    }

    /**
     * 返回錯誤消息
     *
     * @param msg 返回內容
     * @param data 數據對象
     * @return 警告消息
     */
    public static AjaxResult error(String msg, Object data)
    {
        return new AjaxResult(Type.ERROR, msg, data);
    }

    /**
     * 返回錯誤消息（自定義錯誤碼）
     *
     * @param code 錯誤碼
     * @param msg 返回內容
     * @return 錯誤消息
     */
    public static AjaxResult error(int code, String msg)
    {
        AjaxResult result = new AjaxResult();
        result.put(CODE_TAG, code);
        result.put(MSG_TAG, msg);
        return result;
    }

    /**
     * 是否爲成功消息
     *
     * @return 結果
     */
    public boolean isSuccess()
    {
        return Objects.equals(Type.SUCCESS.value, this.get(CODE_TAG));
    }

    /**
     * 是否爲警告消息
     *
     * @return 結果
     */
    public boolean isWarn()
    {
        return Objects.equals(Type.WARN.value, this.get(CODE_TAG));
    }

    /**
     * 是否爲錯誤消息
     *
     * @return 結果
     */
    public boolean isError()
    {
        return Objects.equals(Type.ERROR.value, this.get(CODE_TAG));
    }

    /**
     * 方便鏈式調用
     *
     * @param key 鍵
     * @param value 值
     * @return 數據對象
     */
    @Override
    public AjaxResult put(String key, Object value)
    {
        super.put(key, value);
        return this;
    }
}
