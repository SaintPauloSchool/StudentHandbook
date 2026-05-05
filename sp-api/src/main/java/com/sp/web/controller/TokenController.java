package com.sp.web.controller;

import com.sp.common.annotation.Anonymous;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.system.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Token 相关接口
 */
@RestController
@RequestMapping("/system/token")
public class TokenController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    private TokenService tokenService;

    /**
     * 校验当前 Token 是否有效（未过期）
     * 前端在跳转到外部系统（如校园系统）前可先调用此接口确认 Token 是否仍然有效
     */
    @Anonymous
    @GetMapping("/validate")
    public AjaxResult validate() {
        try {
            String token = getRequest().getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            } else {
                token = getRequest().getParameter("token");
            }
            if (token == null || token.isEmpty()) {
                return AjaxResult.error("token_missing");
            }
            boolean valid = tokenService.validateToken(token);
            if (valid) {
                return AjaxResult.success(token);
            } else {
                return AjaxResult.error("token_expired");
            }
        } catch (Exception e) {
            logger.error("validate token 失败: {}", e.getMessage());
            return AjaxResult.error("token_error");
        }
    }
}
