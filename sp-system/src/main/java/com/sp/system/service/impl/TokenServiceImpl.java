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
    public String createToken(String userId, Integer userType) {
        // 先检查该userId是否存在未过期的token
        Token existingToken = this.tokenMapper.selectValidTokenByUserId(userId);
        // 如果存在未过期的token，返回现有的token
        if (existingToken != null) {
            logger.info("用户userId： {} ， 返回token: {}", userId, existingToken.getToken());
            return existingToken.getToken();
        }
        
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

        logger.info("用户userId： {} ， 生成token: {}", userId, tokenValue);
        return tokenValue;
    }
    
    @Override
    @Transactional
    public String getUserIdByToken(String tokenValue) {
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
        
        return token.getUserId();
    }
}