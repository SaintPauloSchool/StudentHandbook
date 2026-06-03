package com.sp.system.service;

import com.sp.system.entity.Token;

/**
 * TokenService接口
 *
 */
public interface TokenService {
    /**
     * 驗證token是否有效
     *
     * @param tokenValue token值
     * @return 是否有效
     */
    boolean validateToken(String tokenValue);

    /**
     * 創建新的token
     *
     * @param userId 用戶ID
     * @param userType 用戶類型 (1: parent, 0: student, 2: staff)
     * @return token值
     */
    String createToken(String userId, Integer userType);
    
    /**
     * 根據token值獲取用戶ID
     *
     * @param tokenValue token值
     * @return 用戶ID
     */
    String getUserIdByToken(String tokenValue);

    /**
     * 根據token值獲取完整的Token實體
     *
     * @param tokenValue token值
     * @return Token實體，如果無效或過期則返回null
     */
    Token getTokenInfo(String tokenValue);
}