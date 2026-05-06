package com.sp.framework.interceptor;

import com.sp.common.annotation.Anonymous;
import com.sp.common.utils.StringUtils;
import com.sp.system.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * Token 驗證全局攔截器
 * - 對所有非 @Anonymous 接口強制驗證 Token（直查資料庫，確保即時準確）
 * - 驗證通過後將 userId 注入 request attribute，供 Controller 直接取用
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);

    /** Controller 取 userId 用的 attribute key */
    public static final String USER_ID_ATTR = "currentUserId";

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // 放行靜態資源
        if (requestURI.startsWith("/sp-api/assets/") ||
            requestURI.startsWith("/sp-api/dist/") ||
            requestURI.startsWith("/assets/") ||
            requestURI.startsWith("/dist/")) {
            return true;
        }

        // 放行 OPTIONS 預檢請求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 不是映射到方法的請求直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // 有 @Anonymous 注解的接口跳過驗證
        if (method.getAnnotation(Anonymous.class) != null ||
            handlerMethod.getBeanType().getAnnotation(Anonymous.class) != null) {
            return true;
        }

        // 從請求頭取 token（支援 Bearer 格式）
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 驗證 token 是否存在且未過期（直查資料庫，只查一次）
        String userId = null;
        if (StringUtils.isNotEmpty(token)) {
            userId = tokenService.getUserIdByToken(token);
        }

        if (StringUtils.isEmpty(userId)) {
            logger.warn("Token 驗證失敗，請求路徑: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token 無效或已過期，請重新登錄\"}");
            return false;
        }

        // 驗證通過，解析 userId 並注入 request attribute，Controller 直接用 request.getAttribute 取得
        request.setAttribute(USER_ID_ATTR, userId);
        logger.debug("Token 驗證成功，userId: {}，路徑: {}", userId, requestURI);

        return true;
    }
}