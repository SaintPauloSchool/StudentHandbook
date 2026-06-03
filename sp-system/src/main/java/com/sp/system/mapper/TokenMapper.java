package com.sp.system.mapper;

import com.sp.system.entity.Token;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * TokenMapper接口
 *
 */
public interface TokenMapper {
    /**
     * 根據token值查詢Token信息
     *
     * @param tokenValue token值
     * @return Token信息
     */
    Token selectByTokenValue(@Param("tokenValue") String tokenValue);

    /**
     * 根據用戶ID刪除Token
     *
     * @param userId 用戶ID
     * @return 刪除記錄數
     */
    int deleteByUserId(@Param("userId") String userId);
    
    /**
     * 插入Token信息
     * 
     * @param token Token信息
     * @return 插入記錄數
     */
    int insertToken(Token token);
    
    /**
     * 根據ID刪除Token
     * 
     * @param id Token ID
     * @return 刪除記錄數
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根據用戶ID查找未過期的Token
     * 
     * @param userId 用戶ID
     * @param currentTime 當前時間
     * @return Token信息
     */
    Token selectValidTokenByUserId(@Param("userId") String userId, @Param("currentTime") LocalDateTime currentTime);
}