package com.sp.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * 錯誤信息處理類。
 *
 */
public class ExceptionUtil
{
    /**
     * 獲取exception的詳細錯誤信息。
     */
    public static String getExceptionMessage(Throwable e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }

    public static String getRootErrorMessage(Exception e)
    {
        Throwable root = ExceptionUtils.getRootCause(e);
        root = (root == null ? e : root);
        if (root == null)
        {
            return "";
        }
        String msg = root.getMessage();
        if (msg == null)
        {
            return "null";
        }
        return StringUtils.defaultString(msg);
    }

    /**
     * 檢測異常e被觸發的原因是不是因爲異常cause。
     * 
     * @param e 捕獲的異常。
     * @param cause 異常觸發原因。
     * @return 如果異常e是由cause類異常觸發，則返回true；否則返回false。
     */
    public static boolean isCausedBy(final Throwable e, final Class<? extends Throwable> cause)
    {
        if (cause.isAssignableFrom(e.getClass()))
        {
            return true;
        }
        else
        {
            Throwable t = e.getCause();
            while (t != null && t != e)
            {
                if (cause.isAssignableFrom(t.getClass()))
                {
                    return true;
                }
                t = t.getCause();
            }
            return false;
        }
    }
}
