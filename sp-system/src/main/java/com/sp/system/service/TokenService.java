package com.sp.system.service;

/**
 * TokenService接口
 *
 */
public interface TokenService {
    /**
     * 验证token是否有效
     *
     * @param tokenValue token值
     * @return 是否有效
     */
    boolean validateToken(String tokenValue);

    /**
     * 创建新的token
     *
     * @param userId 用户ID
     * @param userType 用户类型 (1: parent, 0: student, 2: staff)
     * @return token值
     */
    String createToken(String userId, Integer userType);
    
    /**
     * 根据token值获取用户ID
     *
     * @param tokenValue token值
     * @return 用户ID
     */
    String getUserIdByToken(String tokenValue);
}