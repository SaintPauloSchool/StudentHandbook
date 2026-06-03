package com.sp.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.sp.common.config.OverallSituationConfig;
import com.sp.common.constant.Constants;
import com.sp.common.utils.http.HttpUtils;

/**
 * 獲取地址類
 *
 */
public class AddressUtils
{
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    // IP地址查詢
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip)
    {
        // 內網不查詢
        if (IpUtils.internalIp(ip))
        {
            return "內網IP";
        }
        if (OverallSituationConfig.isAddressEnabled())
        {
            try
            {
                String rspStr = HttpUtils.sendGet(IP_URL, "ip=" + ip + "&json=true", Constants.GBK);
                if (StringUtils.isEmpty(rspStr))
                {
                    log.error("獲取地理位置異常 {}", ip);
                    return UNKNOWN;
                }
                JSONObject obj = JSONObject.parseObject(rspStr);
                String region = obj.getString("pro");
                String city = obj.getString("city");
                return String.format("%s %s", region, city);
            }
            catch (Exception e)
            {
                log.error("獲取地理位置異常 {}", e);
            }
        }
        return UNKNOWN;
    }
}
