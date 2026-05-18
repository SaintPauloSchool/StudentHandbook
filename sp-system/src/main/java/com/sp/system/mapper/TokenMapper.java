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
     * 根据token值查询Token信息
     *
     * @param tokenValue token值
     * @return Token信息
     */
    Token selectByTokenValue(@Param("tokenValue") String tokenValue);

    /**
     * 根据用户ID删除Token
     *
     * @param userId 用户ID
     * @return 删除记录数
     */
    int deleteByUserId(@Param("userId") String userId);
    
    /**
     * 插入Token信息
     * 
     * @param token Token信息
     * @return 插入记录数
     */
    int insertToken(Token token);
    
    /**
     * 根据ID删除Token
     * 
     * @param id Token ID
     * @return 删除记录数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据用户ID查找未过期的Token
     * 
     * @param userId 用户ID
     * @param currentTime 当前时间
     * @return Token信息
     */
    Token selectValidTokenByUserId(@Param("userId") String userId, @Param("currentTime") LocalDateTime currentTime);
}