package com.sp.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.sp.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * 企業微信OAuth2.0網頁授權工具類
 * 用於企業微信應用的網頁授權功能
 */
@Component
public class WeChatWorkOAuth2Utils {
    
    private static final Logger logger = LoggerFactory.getLogger(WeChatWorkOAuth2Utils.class);
    
    @Value("${wechat.work.corpId}")
    private String corpId;
    
    @Value("${wechat.work.agentId}")
    private String agentId;
    
    @Value("${wechat.work.secret}")
    private String secret;
    
    @Value("${wechat.work.redirectUri}")
    private String redirectUri;
    
    // 修改爲OAuth2授權鏈接
    private static final String AUTHORIZE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize";
    private static final String TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    private static final String USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/school/getuserinfo";
    private static final String AUTH_USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo";
    private static final String USER_GET_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/get";
    
    /**
     * 構造授權鏈接
     * @param state 用於防止csrf攻擊，開發者可以填寫a-zA-Z0-9的參數值，最多128字節
     * @return 授權鏈接
     */
    public String getAuthorizeUrl(String state) {
        StringBuilder urlBuilder = new StringBuilder(AUTHORIZE_URL);
        urlBuilder.append("?appid=").append(corpId);
        try {
            urlBuilder.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("URL編碼失敗", e);
            urlBuilder.append("&redirect_uri=").append(redirectUri);
        }
        urlBuilder.append("&response_type=code");
        urlBuilder.append("&scope=snsapi_base");
        urlBuilder.append("&agentid=").append(agentId);
        urlBuilder.append("&state=").append(state);
        urlBuilder.append("#wechat_redirect");
        logger.info("企業微信授權鏈接: {}", urlBuilder.toString());
        return urlBuilder.toString();
    }
    
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
     * 根據code獲取家校用戶信息（家長或學生）
     * @param code 通過成員授權獲取的code
     * @return 用戶信息（包含parent_userid或student_userid）
     * @throws Exception 獲取失敗時拋出異常
     */
    public JSONObject getSchoolUserInfo(String code) throws Exception {
        String accessToken = getAccessToken();
        
        StringBuilder urlBuilder = new StringBuilder(USER_INFO_URL);
        urlBuilder.append("?access_token=").append(accessToken);
        urlBuilder.append("&code=").append(code);
        
        logger.info("準備獲取家校用戶信息，URL: {}", urlBuilder);
        
        String response = HttpUtils.sendGet(urlBuilder.toString());
        logger.info("獲取家校用戶信息響應長度: {}", response.length());
        
        if (response.isEmpty()) {
            logger.error("獲取家校用戶信息失敗，響應爲空");
            throw new Exception("獲取家校用戶信息失敗，響應爲空");
        }
        
        JSONObject jsonObject = JSONObject.parseObject(response);
        logger.info("獲取家校用戶信息結果: {}", jsonObject.toJSONString());
        return jsonObject;
    }
    
    /**
     * 根據code獲取訪問用戶身份（企業微信成員信息）
     * @param code 通過成員授權獲取的code，最大爲512字節。每次成員授權帶上的code將不一樣，code只能使用一次，5分鐘未被使用自動過期。
     * @return 用戶身份信息（包含userid、openid等）
     * @throws Exception 獲取失敗時拋出異常
     */
    public JSONObject getAuthUserInfo(String code) throws Exception {
        String accessToken = getAccessToken();
        
        StringBuilder urlBuilder = new StringBuilder(AUTH_USER_INFO_URL);
        urlBuilder.append("?access_token=").append(accessToken);
        urlBuilder.append("&code=").append(code);
        
        logger.info("準備獲取訪問用戶身份信息，URL: {}", urlBuilder);
        
        String response = HttpUtils.sendGet(urlBuilder.toString());
        logger.info("獲取訪問用戶身份信息響應長度: {}", response.length());
        
        if (response.isEmpty()) {
            logger.error("獲取訪問用戶身份信息失敗，響應爲空");
            throw new Exception("獲取訪問用戶身份信息失敗，響應爲空");
        }
        
        JSONObject jsonObject = JSONObject.parseObject(response);
        // 只打印返回結果數據，不做其他操作
        logger.info("獲取訪問wecom用戶身份信息結果: {}", jsonObject.toJSONString());
        
        // 如果獲取成功且包含userid，則調用讀取成員接口獲取詳細信息
        if (jsonObject.getInteger("errcode") == 0 && jsonObject.containsKey("userid")) {
            // 獲取userid
            String userid = jsonObject.getString("userid");
            logger.info("獲取到userid: {}，開始調用讀取成員接口", userid);
            
            // 調用讀取成員接口
            StringBuilder userGetUrl = new StringBuilder(USER_GET_URL);
            userGetUrl.append("?access_token=").append(accessToken);
            userGetUrl.append("&userid=").append(userid);
            
            logger.info("準備獲取成員詳細信息，URL: {}", userGetUrl);
            
            String userResponse = HttpUtils.sendGet(userGetUrl.toString());
            logger.info("獲取成員詳細信息響應長度: {}", userResponse.length());
            
            if (!userResponse.isEmpty()) {
                JSONObject userDetail = JSONObject.parseObject(userResponse);
                // 打印返回結果數據
                logger.info("獲取成員詳細信息結果: {}", userDetail.toJSONString());
                
                // 直接返回詳細的成員信息（包含status等字段）
                if (userDetail.getInteger("errcode") == 0) {
                    logger.info("返回詳細成員信息");
                    return userDetail;
                } else {
                    String errorMsg = "獲取成員詳細信息失敗: " + userDetail.getString("errmsg");
                    logger.error(errorMsg);
                    throw new Exception(errorMsg);
                }
            } else {
                logger.error("獲取成員詳細信息失敗，響應爲空");
                throw new Exception("獲取成員詳細信息失敗，響應爲空");
            }
        } else {
            String errorMsg = "獲取訪問用戶身份信息失敗: " + jsonObject.getString("errmsg");
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }
    }
    
    /**
     * 獲取企業ID
     * @return 企業ID
     */
    public String getCorpId() {
        return corpId;
    }
    
    /**
     * 獲取應用ID
     * @return 應用ID
     */
    public String getAgentId() {
        return agentId;
    }
    
    /**
     * 獲取應用密鑰
     * @return 應用密鑰
     */
    public String getSecret() {
        return secret;
    }
    
    /**
     * 獲取回調地址
     * @return 回調地址
     */
    public String getRedirectUri() {
        return redirectUri;
    }
}