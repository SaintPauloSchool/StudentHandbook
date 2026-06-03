package com.sp.framework.interceptor.impl;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import com.sp.common.annotation.RepeatSubmit;
import com.sp.common.json.JSON;
import com.sp.framework.interceptor.RepeatSubmitInterceptor;

/**
 * 判斷請求url和數據是否和上一次相同， 
 * 如果和上次相同，則是重複提交表單。 有效時間爲10秒內。
 *
 */
@Component
public class SameUrlDataInterceptor extends RepeatSubmitInterceptor
{
    public final String REPEAT_PARAMS = "repeatParams";

    public final String REPEAT_TIME = "repeatTime";

    public final String SESSION_REPEAT_KEY = "repeatData";

    @SuppressWarnings("unchecked")
    @Override
    public boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation) throws Exception
    {
        // 本次參數及系統時間
        String nowParams = JSON.marshal(request.getParameterMap());
        Map<String, Object> nowDataMap = new HashMap<String, Object>();
        nowDataMap.put(REPEAT_PARAMS, nowParams);
        nowDataMap.put(REPEAT_TIME, System.currentTimeMillis());

        // 請求地址（作爲存放session的key值）
        String url = request.getRequestURI();

        HttpSession session = request.getSession();
        Object sessionObj = session.getAttribute(SESSION_REPEAT_KEY);
        if (sessionObj != null)
        {
            Map<String, Object> sessionMap = (Map<String, Object>) sessionObj;
            if (sessionMap.containsKey(url))
            {
                Map<String, Object> preDataMap = (Map<String, Object>) sessionMap.get(url);
                if (compareParams(nowDataMap, preDataMap) && compareTime(nowDataMap, preDataMap, annotation.interval()))
                {
                    return true;
                }
            }
        }
        Map<String, Object> sessionMap = new HashMap<String, Object>();
        sessionMap.put(url, nowDataMap);
        session.setAttribute(SESSION_REPEAT_KEY, sessionMap);
        return false;
    }

    /**
     * 判斷參數是否相同
     */
    private boolean compareParams(Map<String, Object> nowMap, Map<String, Object> preMap)
    {
        String nowParams = (String) nowMap.get(REPEAT_PARAMS);
        String preParams = (String) preMap.get(REPEAT_PARAMS);
        return nowParams.equals(preParams);
    }

    /**
     * 判斷兩次間隔時間
     */
    private boolean compareTime(Map<String, Object> nowMap, Map<String, Object> preMap, int interval)
    {
        long time1 = (Long) nowMap.get(REPEAT_TIME);
        long time2 = (Long) preMap.get(REPEAT_TIME);
        if ((time1 - time2) < interval)
        {
            return true;
        }
        return false;
    }
}
