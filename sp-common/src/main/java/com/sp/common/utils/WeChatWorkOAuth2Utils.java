package com.sp.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.sp.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 企業微信 OAuth2 網頁授權工具
 */
@Component
public class WeChatWorkOAuth2Utils {

    private static final Logger logger = LoggerFactory.getLogger(WeChatWorkOAuth2Utils.class);

    private static final String AUTHORIZE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize";
    private static final String TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    private static final String SCHOOL_USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/school/getuserinfo";
    private static final String AUTH_USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo";

    /** 提前刷新，避免邊界過期 */
    private static final long TOKEN_EXPIRE_SKEW_MS = 5 * 60 * 1000L;

    @Value("${wechat.work.corpId}")
    private String corpId;

    @Value("${wechat.work.agentId}")
    private String agentId;

    @Value("${wechat.work.secret}")
    private String secret;

    @Value("${wechat.work.redirectUri}")
    private String redirectUri;

    private volatile String cachedAccessToken;
    private volatile long accessTokenExpireAtMs;

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
        return urlBuilder.toString();
    }

    /**
     * 獲取 access_token（進程內緩存，減少 gettoken 調用）
     */
    public String getAccessToken() throws Exception {
        long now = System.currentTimeMillis();
        String cached = cachedAccessToken;
        if (cached != null && now < accessTokenExpireAtMs) {
            return cached;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            if (cachedAccessToken != null && now < accessTokenExpireAtMs) {
                return cachedAccessToken;
            }
            return refreshAccessToken();
        }
    }

    private String refreshAccessToken() throws Exception {
        String url = TOKEN_URL + "?corpid=" + corpId + "&corpsecret=" + secret;
        logger.info("刷新 access_token, corpid={}", corpId);

        String response = HttpUtils.sendGet(url);
        if (response == null || response.isEmpty()) {
            throw new Exception("獲取access_token失敗，響應爲空");
        }

        JSONObject json = JSONObject.parseObject(response);
        String token = json.getString("access_token");
        if (token == null || token.isEmpty()) {
            throw new Exception("獲取access_token失敗: " + json.getString("errmsg"));
        }

        int expiresIn = json.getIntValue("expires_in");
        if (expiresIn <= 0) {
            expiresIn = 7200;
        }
        cachedAccessToken = token;
        accessTokenExpireAtMs = System.currentTimeMillis() + expiresIn * 1000L - TOKEN_EXPIRE_SKEW_MS;
        logger.info("access_token 已緩存, 有效約 {} 秒", expiresIn);
        return token;
    }

    /** 微信家長/學生身份（消耗 OAuth code） */
    public JSONObject getSchoolUserInfo(String code) throws Exception {
        return exchangeCode(SCHOOL_USER_INFO_URL, code, "school/getuserinfo");
    }

    /** 企微職工身份（消耗 OAuth code） */
    public JSONObject getAuthUserIdentity(String code) throws Exception {
        return exchangeCode(AUTH_USER_INFO_URL, code, "auth/getuserinfo");
    }

    private JSONObject exchangeCode(String apiUrl, String code, String apiName) throws Exception {
        String accessToken = getAccessToken();
        String url = apiUrl + "?access_token=" + accessToken + "&code=" + code;
        logger.info("調用 {}（已隱藏 token/code）", apiName);

        String response = HttpUtils.sendGet(url);
        if (response == null || response.isEmpty()) {
            throw new Exception(apiName + " 響應爲空");
        }

        JSONObject json = JSONObject.parseObject(response);
        logger.info("{} 結果: errcode={}, keys={}", apiName, json.getInteger("errcode"), json.keySet());
        return json;
    }

    public String getCorpId() {
        return corpId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getSecret() {
        return secret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
