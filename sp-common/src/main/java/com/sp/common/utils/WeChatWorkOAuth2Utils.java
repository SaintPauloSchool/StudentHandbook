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
 * 企业微信OAuth2.0网页授权工具类
 * 用于企业微信应用的网页授权功能
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
    
    // 修改为OAuth2授权链接
    private static final String AUTHORIZE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize";
    private static final String TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    private static final String USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/school/getuserinfo";
    private static final String AUTH_USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo";
    private static final String USER_GET_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/get";
    
    /**
     * 构造授权链接
     * @param state 用于防止csrf攻击，开发者可以填写a-zA-Z0-9的参数值，最多128字节
     * @return 授权链接
     */
    public String getAuthorizeUrl(String state) {
        StringBuilder urlBuilder = new StringBuilder(AUTHORIZE_URL);
        urlBuilder.append("?appid=").append(corpId);
        try {
            urlBuilder.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("URL编码失败", e);
            urlBuilder.append("&redirect_uri=").append(redirectUri);
        }
        urlBuilder.append("&response_type=code");
        urlBuilder.append("&scope=snsapi_base");
        urlBuilder.append("&agentid=").append(agentId);
        urlBuilder.append("&state=").append(state);
        urlBuilder.append("#wechat_redirect");
        logger.info("企业微信授权链接: {}", urlBuilder.toString());
        return urlBuilder.toString();
    }
    
    /**
     * 获取Access Token
     * @return Access Token
     * @throws Exception 获取失败时抛出异常
     */
    public String getAccessToken() throws Exception {
        StringBuilder urlBuilder = new StringBuilder(TOKEN_URL);
        urlBuilder.append("?corpid=").append(corpId);
        urlBuilder.append("&corpsecret=").append(secret);
        
        logger.info("准备获取access_token，URL: {}", urlBuilder.toString());
        
        String response = HttpUtils.sendGet(urlBuilder.toString());
        logger.info("获取access_token响应长度: {}", response != null ? response.length() : 0);
        
        if (response == null || response.isEmpty()) {
            logger.error("获取access_token失败，响应为空");
            throw new Exception("获取access_token失败，响应为空");
        }
        
        logger.debug("access_token响应内容(前100字符): {}", response.length() > 100 ? response.substring(0, 100) : response);
        
        JSONObject jsonObject = JSONObject.parseObject(response);
        
        if (jsonObject.containsKey("access_token")) {
            String token = jsonObject.getString("access_token");
            logger.info("成功获取access_token，长度: {}", token != null ? token.length() : 0);
            return token;
        } else {
            String errorMsg = "获取access_token失败: " + jsonObject.getString("errmsg");
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }
    }
    
    /**
     * 根据code获取家校用户信息（家长或学生）
     * @param code 通过成员授权获取的code
     * @return 用户信息（包含parent_userid或student_userid）
     * @throws Exception 获取失败时抛出异常
     */
    public JSONObject getSchoolUserInfo(String code) throws Exception {
        String accessToken = getAccessToken();
        
        StringBuilder urlBuilder = new StringBuilder(USER_INFO_URL);
        urlBuilder.append("?access_token=").append(accessToken);
        urlBuilder.append("&code=").append(code);
        
        logger.info("准备获取家校用户信息，URL: {}", urlBuilder);
        
        String response = HttpUtils.sendGet(urlBuilder.toString());
        logger.info("获取家校用户信息响应长度: {}", response.length());
        
        if (response.isEmpty()) {
            logger.error("获取家校用户信息失败，响应为空");
            throw new Exception("获取家校用户信息失败，响应为空");
        }
        
        JSONObject jsonObject = JSONObject.parseObject(response);
        logger.info("获取家校用户信息结果: {}", jsonObject.toJSONString());
        return jsonObject;
    }
    
    /**
     * 根据code获取访问用户身份（企业微信成员信息）
     * @param code 通过成员授权获取的code，最大为512字节。每次成员授权带上的code将不一样，code只能使用一次，5分钟未被使用自动过期。
     * @return 用户身份信息（包含userid、openid等）
     * @throws Exception 获取失败时抛出异常
     */
    public JSONObject getAuthUserInfo(String code) throws Exception {
        String accessToken = getAccessToken();
        
        StringBuilder urlBuilder = new StringBuilder(AUTH_USER_INFO_URL);
        urlBuilder.append("?access_token=").append(accessToken);
        urlBuilder.append("&code=").append(code);
        
        logger.info("准备获取访问用户身份信息，URL: {}", urlBuilder);
        
        String response = HttpUtils.sendGet(urlBuilder.toString());
        logger.info("获取访问用户身份信息响应长度: {}", response.length());
        
        if (response.isEmpty()) {
            logger.error("获取访问用户身份信息失败，响应为空");
            throw new Exception("获取访问用户身份信息失败，响应为空");
        }
        
        JSONObject jsonObject = JSONObject.parseObject(response);
        // 只打印返回结果数据，不做其他操作
        logger.info("获取访问wecom用户身份信息结果: {}", jsonObject.toJSONString());
        
        // 如果获取成功且包含userid，则调用读取成员接口获取详细信息
        if (jsonObject.getInteger("errcode") == 0 && jsonObject.containsKey("userid")) {
            // 获取userid
            String userid = jsonObject.getString("userid");
            logger.info("获取到userid: {}，开始调用读取成员接口", userid);
            
            // 调用读取成员接口
            StringBuilder userGetUrl = new StringBuilder(USER_GET_URL);
            userGetUrl.append("?access_token=").append(accessToken);
            userGetUrl.append("&userid=").append(userid);
            
            logger.info("准备获取成员详细信息，URL: {}", userGetUrl);
            
            String userResponse = HttpUtils.sendGet(userGetUrl.toString());
            logger.info("获取成员详细信息响应长度: {}", userResponse.length());
            
            if (!userResponse.isEmpty()) {
                JSONObject userDetail = JSONObject.parseObject(userResponse);
                // 打印返回结果数据
                logger.info("获取成员详细信息结果: {}", userDetail.toJSONString());
                
                // 直接返回详细的成员信息（包含status等字段）
                if (userDetail.getInteger("errcode") == 0) {
                    logger.info("返回详细成员信息");
                    return userDetail;
                } else {
                    String errorMsg = "获取成员详细信息失败: " + userDetail.getString("errmsg");
                    logger.error(errorMsg);
                    throw new Exception(errorMsg);
                }
            } else {
                logger.error("获取成员详细信息失败，响应为空");
                throw new Exception("获取成员详细信息失败，响应为空");
            }
        } else {
            String errorMsg = "获取访问用户身份信息失败: " + jsonObject.getString("errmsg");
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }
    }
    
    /**
     * 获取企业ID
     * @return 企业ID
     */
    public String getCorpId() {
        return corpId;
    }
    
    /**
     * 获取应用ID
     * @return 应用ID
     */
    public String getAgentId() {
        return agentId;
    }
    
    /**
     * 获取应用密钥
     * @return 应用密钥
     */
    public String getSecret() {
        return secret;
    }
    
    /**
     * 获取回调地址
     * @return 回调地址
     */
    public String getRedirectUri() {
        return redirectUri;
    }
}