package com.sp.framework.interceptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.sp.common.core.domain.AjaxResult;
import com.sp.common.json.JSON;
import com.sp.common.utils.ServletUtils;
import com.sp.common.utils.security.Md5Utils;

/**
 * API请求校验拦截器
 */
@Component
public class ApiSignatureInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiSignatureInterceptor.class);

    // 缓存 nonces，最大容量 50000，利用 LinkedHashMap 实现简单的 FIFO 淘汰
    private static final Map<String, Boolean> NONCES_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<String, Boolean>(50000, 0.75f, false) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > 50000;
                }
            });

    @Value("${sp.appSecret:y7bUVUsTZ4WufEnF}")
    private String appSecret;

    @Value("${sp.apiTimeRange:300000}")
    private Long apiTimeRange;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 放行 OPTIONS 请求
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return true;
            }

            String xTimestamp = request.getHeader("x-timestamp");
            String xNonces = request.getHeader("x-nonces");
            String xSignature = request.getHeader("x-signature");
            String url = request.getMethod() + " " + request.getRequestURI();

            if (xTimestamp == null) {
                return writeError(response, "request header : x-timestamp is required");
            }
            if (xNonces == null) {
                return writeError(response, "request header : x-nonces is required");
            }
            if (xSignature == null) {
                return writeError(response, "request header : x-signature is required");
            }

            //log.info("---------------request headers => URL[{}], x-timestamp : {} , x-nonces : {}, x-signature : {}", url, xTimestamp, xNonces, xSignature);

            long now = System.currentTimeMillis();
            long reqTime;
            try {
                reqTime = Long.parseLong(xTimestamp);
            } catch (NumberFormatException e) {
                return writeError(response, "x-timestamp format is incorrect");
            }

            if (Math.abs(now - reqTime) > apiTimeRange) {
                log.info("---------------x-timestamp is incorrect => request : {} ,server : {}", xTimestamp, now);
                return writeError(response, "x-timestamp is incorrect");
            }

            // x-nonces 重复校验
            if (NONCES_CACHE.containsKey(xNonces)) {
                log.info("---------------x-nonces is repeated => request : {}", xNonces);
                return writeError(response, "x-nonces is repeated");
            }
            NONCES_CACHE.put(xNonces, true);

            // 校验签名: Md5(AppSecret + timestamp + nonce)
            String signature = Md5Utils.hash(appSecret + xTimestamp + xNonces);
            if (!signature.equalsIgnoreCase(xSignature)) {
                log.info("---------------x-signature is incorrect => request : {} ,server : {}", xSignature, signature);
                return writeError(response, "x-signature is incorrect");
            }

        } catch (Exception e) {
            log.error("Signature Interceptor Exception", e);
            return writeError(response, "signature validation failed: " + e.getMessage());
        }
        return true;
    }

    private boolean writeError(HttpServletResponse response, String msg) {
        AjaxResult ajaxResult = AjaxResult.error(msg);
        try {
            ServletUtils.renderString(response, JSON.marshal(ajaxResult));
        } catch (Exception e) {
            log.error("JSON marshal error", e);
        }
        return false;
    }
}
