package com.sp.web.controller;

import com.sp.common.annotation.Anonymous;
import com.sp.common.core.controller.BaseController;
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
 * 企业微信授权回调
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

    /**
     * 企业微信授权回调处理
     * 此接口专门用于处理企业微信应用的网页授权回调
     *
     * @param code     企业微信授权code
     * @param state    状态码
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

        logger.info("接收到企业微信授权回调，code: {}, state: {}", code, state);

        try {
            // 如果是在开发环境中，直接返回预设的userId生成的Token
            if ("dev".equals(activeProfile)) {
                logger.info("開發環境模擬登錄，使用配置的 userId: {}", devParentUserId);
                // 預設模擬登錄的身份為家長，因此 userType 傳入 1
                String token = tokenService.createToken(devParentUserId, 1);
                logger.info("开发环境模拟登录，生成token: {}", token);
                // 重定向回 dev 前端（帶完整 host:port，避免跳到 prod 的 port 80）
                response.sendRedirect(frontendUrl + "/?token=" + token + "&userType=1");
                return;
            }

            // 对于微信用户测试功能，我们允许特定的state值
            boolean isWechatTest = "wechat_test".equals(state);
            boolean isDefault = "default".equals(state);

            if (!isWechatTest && !isDefault) {
                // 验证state参数，防止CSRF攻击（除了微信测试情况和默认情况）
                String savedState = (String) session.getAttribute("wechat_oauth_state");

                if (savedState == null || !savedState.equals(state)) {
                    logger.warn("state参数验证失败，可能遭遇CSRF攻击");
                    // 返回错误页面
                    response.sendRedirect(frontendUrl + "/login?error=invalid_state");
                    return;
                }
            }

            // 第一步：尝试获取家校用户信息（家长或学生）
            JSONObject userInfo = weChatWorkOAuth2Utils.getSchoolUserInfo(code);
            logger.info("获取企业微信家校用户信息，用户userInfo: {}", userInfo);

            String userId = null;
            int userType = -1; // -1 表示未找到有效用户

            // 检查是否包含家校用户ID（家长或学生）
            if (userInfo.containsKey("parent_userid") || userInfo.containsKey("student_userid")) {
                // 获取家校用户信息成功
                userId = userInfo.containsKey("parent_userid") ? userInfo.getString("parent_userid")
                        : userInfo.getString("student_userid");
                userType = userInfo.containsKey("parent_userid") ? 1 : 0;
                logger.info("处理家校授权用户，用户ID: {}, 类型: {}", userId, userType == 1 ? "家长" : "学生");
            } else {
                // 第二步：如果不是家校用户，尝试获取企业微信员工信息
                logger.info("未找到家校用户信息，尝试获取企业微信员工信息");
                JSONObject employeeInfo = weChatWorkOAuth2Utils.getAuthUserInfo(code);
                logger.info("获取企业微信员工信息，员工info: {}", employeeInfo);

                // 检查是否获取成功且状态为1（已激活）
                if (employeeInfo.getInteger("errcode") == 0 && employeeInfo.containsKey("userid")) {
                    // 获取状态
                    Integer status = employeeInfo.getInteger("status");
                    // 是否已激活
                    if (status != null && status == 1) {
                        userId = employeeInfo.getString("userid");
                        userType = 2; // 员工类型为2
                        logger.info("处理企业微信员工授权，用户ID: {}, 状态: 已激活", userId);
                    } else {
                        logger.warn("企业微信员工状态异常，status: {}", status);
                    }
                } else {
                    logger.error("获取企业微信员工信息失败: {}", employeeInfo.getString("errmsg"));
                }
            }

            // 检查是否获取到有效的用户信息
            if (userId == null) {
                logger.error("无法获取有效的用户信息，授权失败");
                response.sendRedirect(frontendUrl + "/login?error=user_info_failed&message=" +
                        URLEncoder.encode("无法获取用户信息，请联系学校管理员", "UTF-8"));
                return;
            }

            // 清除临时session数据（如果不是测试情况和默认情况）
            if (!isWechatTest && !isDefault) {
                session.removeAttribute("wechat_oauth_state");
            }

            // 如果是家长用户，验证家长是否绑定了学生
            if (userType == 1) {
                // 验证家长是否绑定了学生（检查是否在sys_department_parent_binding表中有绑定学生）
                if (!departmentParentBindingService.checkHasBoundStudents(userId)) {
                    logger.warn("家长用户 {} 不存在有效的学生关联，授权失败", userId);
                    // 重定向到错误页面
                    response.sendRedirect(frontendUrl + "/login?error=authorization_failed&message=" +
                            URLEncoder.encode("家长账户未关联任何学生，请联系学校管理员确认", "UTF-8"));
                    return;
                }
            }

            // 生成Token
            String token = tokenService.createToken(userId, userType);
            logger.info("用户 {} (类型: {}) 登录成功，生成token: {}", userId, 
                    userType == 0 ? "学生" : (userType == 1 ? "家长" : "员工"), token);

            // 重定向到前端页面并带上 userType，方便前端立刻决定显示哪些按钮
            response.sendRedirect(frontendUrl + "/?token=" + token + "&userType=" + userType);
        } catch (Exception e) {
            logger.error("处理企业微信家校授权回调时发生错误", e);
            try {
                // 重定向到错误页面
                response.sendRedirect(frontendUrl + "/login?error=internal_error&message=" +
                        URLEncoder.encode(e.getMessage(), "UTF-8"));
            } catch (IOException ioException) {
                logger.error("重定向失败", ioException);
            }
        }
    }
}