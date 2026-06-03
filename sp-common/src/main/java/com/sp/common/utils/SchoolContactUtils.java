package com.sp.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.sp.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 家校通訊錄工具類
 * 用於獲取學生或家長的身份信息
 */
public class SchoolContactUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SchoolContactUtils.class);
    
    /**
     * 企業微信API地址
     */
    private static final String GET_USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/school/user/get";
    
    /**
     * 根據用戶ID獲取學生或家長信息
     * @param accessToken 企業微信應用的access_token
     * @param userId 用戶ID（學生或家長的userid）
     * @return 用戶信息對象
     */
    public static JSONObject getUserInfo(String accessToken, String userId) {
        try {
            StringBuilder urlBuilder = new StringBuilder(GET_USER_INFO_URL);
            urlBuilder.append("?access_token=").append(accessToken);
            urlBuilder.append("&userid=").append(userId);
            
            String response = HttpUtils.sendGet(urlBuilder.toString());
            logger.info("獲取家校通訊錄用戶信息結果: {}", response);
            
            JSONObject result = JSONObject.parseObject(response);
            logger.info("解析後的JSON對象: {}", result.toJSONString());
            
            return result;
        } catch (Exception e) {
            logger.error("獲取家校通訊錄用戶信息異常", e);
            return null;
        }
    }
    
    /**
     * 判斷用戶是否爲學生
     * @param userInfo 用戶信息對象
     * @return true表示是學生，false表示不是學生（可能是家長或其他）
     */
    public static boolean isStudent(JSONObject userInfo) {
        if (userInfo == null || !userInfo.containsKey("student")) {
            return false;
        }
        return true;
    }
    
    /**
     * 判斷用戶是否爲家長
     * @param userInfo 用戶信息對象
     * @return true表示是家長，false表示不是家長（可能是學生或其他）
     */
    public static boolean isParent(JSONObject userInfo) {
        if (userInfo == null || !userInfo.containsKey("parent")) {
            return false;
        }
        return true;
    }
    
    /**
     * 獲取學生信息
     * @param userInfo 用戶信息對象
     * @return 學生信息對象
     */
    public static JSONObject getStudentInfo(JSONObject userInfo) {
        if (userInfo != null && userInfo.containsKey("student")) {
            return userInfo.getJSONObject("student");
        }
        return null;
    }
    
    /**
     * 獲取家長信息
     * @param userInfo 用戶信息對象
     * @return 家長信息對象
     */
    public static JSONObject getParentInfo(JSONObject userInfo) {
        if (userInfo != null && userInfo.containsKey("parent")) {
            return userInfo.getJSONObject("parent");
        }
        return null;
    }
}