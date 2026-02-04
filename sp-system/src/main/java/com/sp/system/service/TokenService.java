package com.sp.system.service;

import javax.servlet.http.HttpServletRequest;

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
     * @return token值
     */
    String createToken(Long userId);

    /**
     * 为家长用户创建token
     *
     * @param userId 用户ID
     * @param parentUserId 家长用户ID
     * @return token值
     */
    String createTokenWithParentUserId(Long userId, String parentUserId);
    
    /**
     * 根据token值获取家长用户ID
     *
     * @param tokenValue token值
     * @return 家长用户ID
     */
    String getParentUserIdByToken(String tokenValue);

    /**
     * 从HTTP请求中验证token并获取家长用户ID
     *
     * @param request HTTP请求
     * @return 家长用户ID，验证失败返回null
     */
    String getParentUserIdFromRequest(HttpServletRequest request);
}