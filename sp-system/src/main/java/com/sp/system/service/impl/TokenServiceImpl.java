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

import java.time.LocalDateTime;

/**
 * TokenService實現類
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
        return getTokenInfo(tokenValue) != null;
    }

    @Override
    @Transactional
    public String createToken(String userId, Integer userType) {
        // 先檢查該userId是否存在未過期的token
        Token existingToken = this.tokenMapper.selectValidTokenByUserId(userId, LocalDateTime.now());
        // 如果存在未過期的token，返回現有的token
        if (existingToken != null) {
            logger.info("用戶userId： {} ， 返回token: {}", userId, existingToken.getToken());
            return existingToken.getToken();
        }
        
        // 先刪除該用戶之前的token
        this.tokenMapper.deleteByUserId(userId);

        // 創建新token
        String tokenValue = UUID.randomUUID().toString();

        Token token = new Token();
        token.setUserId(userId);
        token.setUserType(userType);
        token.setToken(tokenValue);
        token.setCreateTime(LocalDateTime.now());
        token.setUpdateTime(LocalDateTime.now());
        token.setExpireTime(LocalDateTime.now().plusDays(expireTimeInDays));

        // 使用自定義的insertToken方法
        this.tokenMapper.insertToken(token);

        logger.info("用戶userId： {} ， 生成token: {}", userId, tokenValue);
        return tokenValue;
    }
    
    @Override
    @Transactional
    public String getUserIdByToken(String tokenValue) {
        Token token = getTokenInfo(tokenValue);
        return token != null ? token.getUserId() : null;
    }

    @Override
    @Transactional
    public Token getTokenInfo(String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return null;
        }
        
        Token token = this.tokenMapper.selectByTokenValue(tokenValue);
        if (token == null) {
            return null;
        }
        
        // 檢查token是否過期
        if (token.getExpireTime().isBefore(LocalDateTime.now())) {
            // 刪除過期token
            this.tokenMapper.deleteById(token.getId());
            return null;
        }
        
        return token;
    }
}