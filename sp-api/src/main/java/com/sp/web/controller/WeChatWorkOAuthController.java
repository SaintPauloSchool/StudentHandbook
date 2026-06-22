package com.sp.web.controller;

import com.sp.common.annotation.Anonymous;
import com.sp.common.core.controller.BaseController;
import com.sp.common.utils.StringUtils;
import com.sp.common.utils.WeChatWorkOAuth2Utils;
import com.alibaba.fastjson.JSONObject;

import com.sp.system.service.TokenService;
import com.sp.system.service.DepartmentParentBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * 企業微信授權回調
 */
@RestController
@RequestMapping("/wechat/oauth")
public class WeChatWorkOAuthController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(WeChatWorkOAuthController.class);

    @Autowired
    private WeChatWorkOAuth2Utils weChatWorkOAuth2Utils;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private DepartmentParentBindingService departmentParentBindingService;

    @Value("${sp.token.parentUserId}")
    private String devParentUserId;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${sp.frontend.url}")
    private String frontendUrl;

    @Value("${sp.frontend.version:}")
    private String frontendVersion;

    private String buildFrontendRedirect(String pathWithQuery) throws IOException {
        String base = frontendUrl.replaceAll("/+$", "");
        if (StringUtils.isNotEmpty(frontendVersion)) {
            base = base + "/" + frontendVersion.trim();
        }
        if (pathWithQuery == null || pathWithQuery.isEmpty()) {
            pathWithQuery = "/";
        } else if (!pathWithQuery.startsWith("/")) {
            pathWithQuery = "/" + pathWithQuery;
        }
        return base + pathWithQuery;
    }

    private void redirectToFrontend(HttpServletResponse response, String pathWithQuery) throws IOException {
        response.sendRedirect(buildFrontendRedirect(pathWithQuery));
    }

    /**
     * 企業微信授權回調處理
     * 此接口專門用於處理企業微信應用的網頁授權回調
     *
     * @param code     企業微信授權code
     * @param state    狀態碼
     * @param session  HttpSession
     * @param response HttpServletResponse
     */
    @Anonymous
    @GetMapping("/callback")
    public void callback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session,
            HttpServletResponse response) {

        logger.info("接收到企業微信授權回調，code: {}, state: {}", code, state);

        try {
            // 如果是在開發環境中，直接返回預設的userId生成的Token
            if ("dev".equals(activeProfile)) {
                logger.info("開發環境模擬登錄，使用配置的 userId: {}", devParentUserId);
                // 預設模擬登錄的身份為家長，因此 userType 傳入 1
                String token = tokenService.createToken(devParentUserId, 1);
                logger.info("開發環境模擬登錄，生成token: {}", token);
                // 重定向回 dev 前端（帶完整 host:port，避免跳到 prod 的 port 80）
                response.sendRedirect(buildFrontendRedirect("/?token=" + token + "&userType=1"));
                return;
            }

            boolean isWechatTest = "wechat_test".equals(state);
            boolean isDefault = "default".equals(state);
            boolean isCampusNotice = state != null && state.startsWith("campus_notice_");

            if (!isWechatTest && !isDefault && !isCampusNotice) {
                // 驗證state參數，防止CSRF攻擊（除了微信測試情況、默認情況、和校園系統抄送通知）
                String savedState = (String) session.getAttribute("wechat_oauth_state");

                if (savedState == null || !savedState.equals(state)) {
                    logger.warn("state參數驗證失敗，可能遭遇CSRF攻擊");
                    // 返回錯誤頁面
                    redirectToFrontend(response, "/login?error=invalid_state");
                    return;
                }
            }

            // 第一步：嘗試獲取家校用戶信息（家長或學生）
            JSONObject userInfo = weChatWorkOAuth2Utils.getSchoolUserInfo(code);
            logger.info("獲取企業微信家校用戶信息，用戶userInfo: {}", userInfo);

            String userId = null;
            int userType = -1; // -1 表示未找到有效用戶

            // 檢查是否包含家校用戶ID（家長或學生）
            if (userInfo.containsKey("parent_userid") || userInfo.containsKey("student_userid")) {
                // 獲取家校用戶信息成功
                userId = userInfo.containsKey("parent_userid") ? userInfo.getString("parent_userid")
                        : userInfo.getString("student_userid");
                userType = userInfo.containsKey("parent_userid") ? 1 : 0;
                logger.info("處理家校授權用戶，用戶ID: {}, 類型: {}", userId, userType == 1 ? "家長" : "學生");
            } else {
                // 第二步：如果不是家校用戶，嘗試獲取企業微信員工信息
                logger.info("未找到家校用戶信息，嘗試獲取企業微信員工信息");
                JSONObject employeeInfo = weChatWorkOAuth2Utils.getAuthUserInfo(code);
                logger.info("獲取企業微信員工信息，員工info: {}", employeeInfo);

                // 檢查是否獲取成功且狀態爲1（已激活）
                if (employeeInfo.getInteger("errcode") == 0 && employeeInfo.containsKey("userid")) {
                    // 獲取狀態
                    Integer status = employeeInfo.getInteger("status");
                    // 是否已激活
                    if (status != null && status == 1) {
                        userId = employeeInfo.getString("userid");
                        userType = 2; // 員工類型爲2
                        logger.info("處理企業微信員工授權，用戶ID: {}, 狀態: 已激活", userId);
                    } else {
                        logger.warn("企業微信員工狀態異常，status: {}", status);
                    }
                } else {
                    logger.error("獲取企業微信員工信息失敗: {}", employeeInfo.getString("errmsg"));
                }
            }

            // 檢查是否獲取到有效的用戶信息
            if (userId == null) {
                logger.error("無法獲取有效的用戶信息，授權失敗");
                redirectToFrontend(response, "/login?error=user_info_failed&message=" +
                        URLEncoder.encode("無法獲取用戶信息，請聯繫學校管理員", "UTF-8"));
                return;
            }

            // 清除臨時session數據（如果不是測試情況、默認情況和校園通知情況）
            if (!isWechatTest && !isDefault && !isCampusNotice) {
                session.removeAttribute("wechat_oauth_state");
            }

            // 如果是家長用戶，驗證家長是否綁定了學生
            if (userType == 1) {
                // 驗證家長是否綁定了學生（檢查是否在sys_department_parent_binding表中有綁定學生）
                if (!departmentParentBindingService.checkHasBoundStudents(userId)) {
                    logger.warn("家長用戶 {} 不存在有效的學生關聯，授權失敗", userId);
                    // 重定向到錯誤頁面
                    redirectToFrontend(response, "/login?error=authorization_failed&message=" +
                            URLEncoder.encode("家長賬戶未關聯任何學生，請聯繫學校管理員確認", "UTF-8"));
                    return;
                }
            }

            // 生成Token
            String token = tokenService.createToken(userId, userType);
            logger.info("用戶 {} (類型: {}) 登錄成功，生成token: {}", userId,
                    userType == 0 ? "學生" : (userType == 1 ? "家長" : "員工"), token);

            // 重定向到前端頁面並帶上 userType，方便前端立刻決定顯示哪些按鈕。同時帶上 state 以便處理通知跳轉
            String redirectUrl = buildFrontendRedirect("/?token=" + token + "&userType=" + userType);
            if (state != null && !state.isEmpty()) {
                redirectUrl += "&state=" + URLEncoder.encode(state, "UTF-8");
            }
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            logger.error("處理企業微信家校授權回調時發生錯誤", e);
            try {
                // 重定向到錯誤頁面
                redirectToFrontend(response, "/login?error=internal_error&message=" +
                        URLEncoder.encode(e.getMessage(), "UTF-8"));
            } catch (IOException ioException) {
                logger.error("重定向失敗", ioException);
            }
        }
    }
}