package com.sp.web.controller;

import com.sp.common.annotation.Anonymous;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.system.entity.Token;
import com.sp.system.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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
            Token tokenEntity = tokenService.getTokenInfo(token);
            if (tokenEntity != null) {
                // 安全考量：不要把整個資料庫實體（包含ID、建立時間等）丟給前端，只回傳必要的資訊
                Map<String, Object> safeData = new HashMap<>();
                safeData.put("userType", tokenEntity.getUserType());
                return AjaxResult.success(safeData);
            } else {
                return AjaxResult.error("token_expired");
            }
        } catch (Exception e) {
            logger.error("validate token 失败: {}", e.getMessage());
            return AjaxResult.error("token_error");
        }
    }
}
