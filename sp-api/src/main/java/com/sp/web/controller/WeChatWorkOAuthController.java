package com.sp.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.sp.common.annotation.Anonymous;
import com.sp.common.core.controller.BaseController;
import com.sp.common.utils.WeChatWorkOAuth2Utils;
import com.sp.system.service.ISchoolFamilyContactService;
import com.sp.system.service.IWecomSchoolDepartmentMemberService;
import com.sp.system.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 企業微信 OAuth 回調。
 * <p>
 * code 只能兌換一次，必須按入口分流（前端 state 前綴）：
 * <ul>
 *   <li>wecom* → auth/getuserinfo → wecom_school_department_member → 職工</li>
 *   <li>其餘（wechat* / default 等）→ school/getuserinfo → 家校通訊錄 → 家長/學生</li>
 * </ul>
 */
@RestController
@RequestMapping("/wechat/oauth")
public class WeChatWorkOAuthController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(WeChatWorkOAuthController.class);

    private static final ConcurrentHashMap<String, Long> USED_OAUTH_CODES = new ConcurrentHashMap<>();
    private static final long OAUTH_CODE_TTL_MS = 5 * 60 * 1000L;

    private static final int USER_TYPE_STUDENT = 0;
    private static final int USER_TYPE_PARENT = 1;
    private static final int USER_TYPE_STAFF = 2;

    @Autowired
    private WeChatWorkOAuth2Utils weChatWorkOAuth2Utils;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISchoolFamilyContactService schoolFamilyContactService;

    @Autowired
    private IWecomSchoolDepartmentMemberService wecomSchoolDepartmentMemberService;

    @Value("${sp.token.parentUserId}")
    private String devParentUserId;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${sp.frontend.url}")
    private String frontendUrl;

    @Anonymous
    @GetMapping("/callback")
    public void callback(
            @RequestParam String code,
            @RequestParam(defaultValue = "wechat") String state,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        logger.info("OAuth 回調: code前綴={}, len={}, state={}, profile={}",
                abbreviateCode(code), code != null ? code.length() : 0, state, activeProfile);

        try {
            if ("dev".equals(activeProfile)) {
                String token = tokenService.createToken(devParentUserId, USER_TYPE_PARENT);
                redirectWithToken(response, token, USER_TYPE_PARENT, state);
                return;
            }

            if ("dev".equals(code)) {
                fail(response, "invalid_code", "授權參數錯誤，請從微信/企微重新進入");
                return;
            }

            if (!markCodeUsed(code)) {
                fail(response, "invalid_code", "授權碼已使用，請關閉後重新進入系統");
                return;
            }

            if (!isTrustedState(state)) {
                String savedState = (String) session.getAttribute("wechat_oauth_state");
                if (savedState == null || !savedState.equals(state)) {
                    logger.warn("state 校驗失敗: {}", state);
                    fail(response, "invalid_state", "授權狀態校驗失敗，請重新進入系統");
                    return;
                }
                session.removeAttribute("wechat_oauth_state");
            }

            LoginIdentity identity = isWecomChannel(state)
                    ? resolveStaff(code, response)
                    : resolveFamily(code, response);
            if (identity == null) {
                return;
            }

            String token = tokenService.createToken(identity.userId, identity.userType);
            logger.info("登錄成功: userId={}, userType={}", identity.userId, identity.userType);
            redirectWithToken(response, token, identity.userType, state);
        } catch (Exception e) {
            logger.error("OAuth 回調異常", e);
            fail(response, "internal_error",
                    e.getMessage() != null ? e.getMessage() : "系統內部錯誤");
        }
    }

    /** 企微職工：auth/getuserinfo + wecom_school_department_member */
    private LoginIdentity resolveStaff(String code, HttpServletResponse response) throws Exception {
        logger.info("入口=企微職工 → auth/getuserinfo");
        JSONObject authInfo = weChatWorkOAuth2Utils.getAuthUserIdentity(code);

        if (!isApiOk(authInfo)) {
            Integer err = authInfo.getInteger("errcode");
            logger.error("auth/getuserinfo 失敗: errcode={}, errmsg={}", err, authInfo.getString("errmsg"));
            fail(response, "user_info_failed", tipForErr(err, true));
            return null;
        }

        String userId = firstNonEmpty(authInfo.getString("userid"), authInfo.getString("UserId"));
        if (userId == null) {
            logger.error("auth/getuserinfo 未返回 userid: {}", authInfo);
            fail(response, "user_info_failed", "無法識別企微用戶身份，請聯繫學校管理員");
            return null;
        }

        if (!wecomSchoolDepartmentMemberService.checkIsDepartmentMember(userId)) {
            logger.warn("職工 {} 不在 wecom_school_department_member", userId);
            fail(response, "authorization_failed", "您的企微賬戶未在學校部門成員中，請聯繫管理員確認");
            return null;
        }

        logger.info("識別為企微職工, userId={}", userId);
        return new LoginIdentity(userId, USER_TYPE_STAFF);
    }

    /** 微信家長/學生：school/getuserinfo + 家校通訊錄 */
    private LoginIdentity resolveFamily(String code, HttpServletResponse response) throws Exception {
        logger.info("入口=微信家校 → school/getuserinfo");
        JSONObject userInfo = weChatWorkOAuth2Utils.getSchoolUserInfo(code);

        if (!isApiOk(userInfo)) {
            Integer err = userInfo.getInteger("errcode");
            logger.error("school/getuserinfo 失敗: errcode={}, errmsg={}", err, userInfo.getString("errmsg"));
            fail(response, "user_info_failed", tipForErr(err, false));
            return null;
        }

        String parentId = userInfo.getString("parent_userid");
        String studentId = userInfo.getString("student_userid");

        if (parentId != null && !parentId.isEmpty()) {
            if (!schoolFamilyContactService.checkHasBoundStudents(parentId)) {
                logger.warn("家長 {} 無學生關聯", parentId);
                fail(response, "authorization_failed", "家長賬戶未關聯任何學生，請聯繫學校管理員確認");
                return null;
            }
            logger.info("識別為家長, userId={}", parentId);
            return new LoginIdentity(parentId, USER_TYPE_PARENT);
        }

        if (studentId != null && !studentId.isEmpty()) {
            logger.info("識別為學生, userId={}", studentId);
            return new LoginIdentity(studentId, USER_TYPE_STUDENT);
        }

        logger.error("school/getuserinfo 無家長/學生身份: keys={}", userInfo.keySet());
        fail(response, "user_info_failed", "無法識別用戶身份，請確認已綁定家校家長後重試");
        return null;
    }

    private boolean isWecomChannel(String state) {
        return state != null && state.startsWith("wecom");
    }

    /**
     * 前端約定的 state 無需 session CSRF；
     * 兼容舊版 default / campus_notice_* / wechat_test。
     */
    private boolean isTrustedState(String state) {
        if (state == null || state.isEmpty()) {
            return false;
        }
        return "default".equals(state)
                || "wechat_test".equals(state)
                || state.startsWith("wecom")
                || state.startsWith("wechat")
                || state.startsWith("campus_notice_")
                || state.contains("_campus_notice_");
    }

    private boolean markCodeUsed(String code) {
        purgeExpiredOauthCodes();
        Long firstSeen = USED_OAUTH_CODES.putIfAbsent(code, System.currentTimeMillis());
        if (firstSeen != null) {
            logger.warn("同一 OAuth code 重複回調（距首次 {}ms）", System.currentTimeMillis() - firstSeen);
            return false;
        }
        return true;
    }

    private void purgeExpiredOauthCodes() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = USED_OAUTH_CODES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> e = it.next();
            if (now - e.getValue() > OAUTH_CODE_TTL_MS) {
                it.remove();
            }
        }
    }

    private boolean isApiOk(JSONObject json) {
        if (json == null) {
            return false;
        }
        Integer err = json.getInteger("errcode");
        return err == null || err == 0;
    }

    private String tipForErr(Integer errcode, boolean wecom) {
        if (errcode != null && errcode == 40029) {
            return wecom
                    ? "授權碼無效或已被使用，請勿刷新頁面，請從企微重新進入系統"
                    : "授權碼無效或已被使用，請勿刷新頁面，請從微信重新進入系統";
        }
        return wecom ? "企微授權失敗，請重新進入系統" : "微信授權失敗，請重新進入系統";
    }

    private void redirectWithToken(HttpServletResponse response, String token, int userType, String state)
            throws IOException {
        StringBuilder url = new StringBuilder(frontendBase());
        url.append("/?token=").append(urlEncode(token));
        url.append("&userType=").append(userType);
        if (state != null && !state.isEmpty()) {
            url.append("&state=").append(urlEncode(state));
        }
        response.sendRedirect(url.toString());
    }

    private void fail(HttpServletResponse response, String error, String message) throws IOException {
        String url = frontendBase() + "/login?error=" + urlEncode(error)
                + "&message=" + urlEncode(message);
        response.sendRedirect(url);
    }

    private String frontendBase() {
        return frontendUrl.replaceAll("/+$", "");
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private static String abbreviateCode(String code) {
        if (code == null) {
            return null;
        }
        return code.length() > 6 ? code.substring(0, 6) + "..." : code;
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.isEmpty()) {
            return a;
        }
        if (b != null && !b.isEmpty()) {
            return b;
        }
        return null;
    }

    private static final class LoginIdentity {
        private final String userId;
        private final int userType;

        private LoginIdentity(String userId, int userType) {
            this.userId = userId;
            this.userType = userType;
        }
    }
}
