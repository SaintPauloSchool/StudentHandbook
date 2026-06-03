package com.sp.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.sp.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 企業微信家校應用工具類
 * 用於獲取家校場景下的用戶身份信息
 */
@Component
public class WeChatWorkSchoolUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(WeChatWorkSchoolUtils.class);
    
    @Value("${wechat.work.corpId}")
    private String corpId;
    
    @Value("${wechat.work.secret}")
    private String secret;
    
    // 家校獲取用戶信息接口
    private static final String TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    private static final String SCHOOL_USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/school/getuserinfo";
    private static final String SCHOOL_USER_DETAIL_URL = "https://qyapi.weixin.qq.com/cgi-bin/school/user/get";
    
    /**
     * 獲取Access Token
     * @return Access Token
     * @throws Exception 獲取失敗時拋出異常
     */
    public String getAccessToken() throws Exception {
        StringBuilder urlBuilder = new StringBuilder(TOKEN_URL);
        urlBuilder.append("?corpid=").append(corpId);
        urlBuilder.append("&corpsecret=").append(secret);
        
        logger.info("準備獲取access_token，URL: {}", urlBuilder.toString());
        
        String response = HttpUtils.sendGet(urlBuilder.toString());
        logger.info("獲取access_token響應長度: {}", response != null ? response.length() : 0);
        
        if (response == null || response.isEmpty()) {
            logger.error("獲取access_token失敗，響應爲空");
            throw new Exception("獲取access_token失敗，響應爲空");
        }
        
        logger.debug("access_token響應內容(前100字符): {}", response.length() > 100 ? response.substring(0, 100) : response);
        
        JSONObject jsonObject = JSONObject.parseObject(response);
        
        if (jsonObject.containsKey("access_token")) {
            String token = jsonObject.getString("access_token");
            logger.info("成功獲取access_token，長度: {}", token != null ? token.length() : 0);
            return token;
        } else {
            String errorMsg = "獲取access_token失敗: " + jsonObject.getString("errmsg");
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }
    }

    /**
     * 根據userid獲取家校用戶詳細信息（家長或學生）
     * @param userid 家校通訊錄的userid
     * @return 用戶詳細信息
     * @throws Exception 獲取失敗時拋出異常
     */
    public JSONObject getSchoolUserDetail(String userid) throws Exception {
        String accessToken = getAccessToken();
        
        StringBuilder urlBuilder = new StringBuilder(SCHOOL_USER_DETAIL_URL);
        urlBuilder.append("?access_token=").append(accessToken);
        urlBuilder.append("&userid=").append(userid);
        
        logger.info("準備獲取家校用戶詳細信息，URL: {}", urlBuilder.toString());
        
        String response = HttpUtils.sendGet(urlBuilder.toString());
        logger.info("獲取家校用戶詳細信息響應長度: {}", response != null ? response.length() : 0);
        
        if (response == null || response.isEmpty()) {
            logger.error("獲取家校用戶詳細信息失敗，響應爲空");
            throw new Exception("獲取家校用戶詳細信息失敗，響應爲空");
        }
        
        JSONObject jsonObject = JSONObject.parseObject(response);
        logger.info("獲取家校用戶詳細信息結果: {}", jsonObject.toJSONString());
        return jsonObject;
    }
}