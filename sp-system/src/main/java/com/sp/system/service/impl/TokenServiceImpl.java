package com.sp.system.service.impl;

import com.sp.system.entity.Token;
import com.sp.system.mapper.TokenMapper;
import com.sp.system.service.TokenService;
import com.sp.common.utils.uuid.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * TokenService实现类
 *
 */
@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Autowired
    private TokenMapper tokenMapper;
    
    @Value("${sp.token.expireTime}")
    private int expireTimeInDays;

    @Override
    @Transactional
    public boolean validateToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return false;
        }

        Token token = this.tokenMapper.selectByTokenValue(tokenValue);
        if (token == null) {
            return false;
        }

        // 检查token是否过期
        if (token.getExpireTime().isBefore(LocalDateTime.now())) {
            // 删除过期token
            this.tokenMapper.deleteById(token.getId());
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public String createToken(Long userId, Integer userType) {
        // 先删除该用户之前的token
        this.tokenMapper.deleteByUserId(userId);

        // 创建新token
        String tokenValue = UUID.randomUUID().toString();

        Token token = new Token();
        token.setUserId(userId);
        token.setUserType(userType);
        token.setToken(tokenValue);
        token.setCreateTime(LocalDateTime.now());
        token.setUpdateTime(LocalDateTime.now());
        token.setExpireTime(LocalDateTime.now().plusDays(expireTimeInDays));

        // 使用自定义的insertToken方法
        this.tokenMapper.insertToken(token);

        return tokenValue;
    }
    
    /**
     * 为家长用户创建token
     *
     * @param userId 用户ID
     * @param parentUserId 家长用户ID
     * @param userType 用户类型 (1: parent, 0: student)
     * @return token值
     */
    @Transactional
    public String createTokenWithParentUserId(Long userId, String parentUserId, Integer userType) {
        // 先检查该parentUserId是否存在未过期的token
        Token existingToken = this.tokenMapper.selectValidTokenByParentUserId(parentUserId);
        // 如果存在未过期的token，返回现有的token
        if (existingToken != null) {
            logger.info("用户parentUserId： {} ， 返回token: {}", parentUserId, existingToken.getToken());
            return existingToken.getToken();
        }
        
        // 先删除该用户之前的token
        this.tokenMapper.deleteByUserId(userId);

        // 创建新token
        String tokenValue = UUID.randomUUID().toString();

        Token token = new Token();
        token.setUserId(userId);
        token.setParentUserId(parentUserId);
        token.setUserType(userType);
        token.setToken(tokenValue);
        token.setCreateTime(LocalDateTime.now());
        token.setUpdateTime(LocalDateTime.now());
        token.setExpireTime(LocalDateTime.now().plusDays(expireTimeInDays));

        // 使用自定义的insertToken方法
        this.tokenMapper.insertToken(token);

        logger.info("用户parentUserId： {} ， 生成token: {}", parentUserId, tokenValue);
        return tokenValue;
    }
    
    @Override
    @Transactional
    public String getParentUserIdByToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return null;
        }
        
        Token token = this.tokenMapper.selectByTokenValue(tokenValue);
        if (token == null) {
            return null;
        }
        
        // 检查token是否过期
        if (token.getExpireTime().isBefore(LocalDateTime.now())) {
            // 删除过期token
            this.tokenMapper.deleteById(token.getId());
            return null;
        }
        
        return token.getParentUserId();
    }
}