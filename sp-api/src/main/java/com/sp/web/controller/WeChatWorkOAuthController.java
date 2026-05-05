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
            // 如果是在开发环境中，直接返回预设的parentUserId生成的Token
            if ("dev".equals(activeProfile)) {
                logger.info("開發環境模擬登錄，使用配置的 parentUserId: {}", devParentUserId);
                long numericUserId = devParentUserId.hashCode();
                if (numericUserId < 0) {
                    numericUserId = Math.abs(numericUserId);
                }
                if (numericUserId == 0) {
                    numericUserId = System.currentTimeMillis();
                }
                String token = tokenService.createTokenWithParentUserId(numericUserId, devParentUserId);
                logger.info("开发环境模拟登录，生成token: {}", token);
                response.sendRedirect("/?token=" + token);
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
                    response.sendRedirect("/login?error=invalid_state");
                    return;
                }
            }

            // 根据code获取家校用户信息（家长或学生）
            JSONObject userInfo = weChatWorkOAuth2Utils.getSchoolUserInfo(code);

            logger.info("获取企业微信家校用户信息，用户userInfo: {}", userInfo);

            // 检查是否包含家长(parent_userid)或学生(student_userid)信息
            if (userInfo.containsKey("parent_userid") || userInfo.containsKey("student_userid")) {
                // 获取用户信息成功
                String userId = userInfo.containsKey("parent_userid") ? userInfo.getString("parent_userid")
                        : userInfo.getString("student_userid");
                logger.info("處理授權用戶，用戶ID: {}", userId);

                // 清除临时session数据（如果不是测试情况和默认情况）
                if (!isWechatTest && !isDefault) {
                    session.removeAttribute("wechat_oauth_state");
                }

                // 生成token（使用用户ID的哈希值作为用户ID，因为原ID可能是字符串）
                // 这里我们使用一个简单的哈希算法来生成数字ID
                long numericUserId = userId.hashCode();
                if (numericUserId < 0) {
                    numericUserId = Math.abs(numericUserId);
                }
                if (numericUserId == 0) {
                    numericUserId = System.currentTimeMillis();
                }

                String token;
                // 如果是家长用户（在userInfo中直接检查），使用带有parentUserId的方法创建token
                if (userInfo.containsKey("parent_userid")) {
                    // 家长用户id
                    String parentUserId = userInfo.getString("parent_userid");
                    // 验证家长是否绑定了学生（检查是否在sys_department_parent_binding表中有绑定学生）
                    if (!departmentParentBindingService.checkHasBoundStudents(parentUserId)) {
                        logger.warn("家长用户 {} 不存在有效的学生关联，授权失败", parentUserId);
                        // 重定向到错误页面
                        response.sendRedirect("/login?error=authorization_failed&message=" +
                                java.net.URLEncoder.encode("家长账户未关联任何学生，请联系学校管理员确认", "UTF-8"));
                        return;
                    }

                    token = tokenService.createTokenWithParentUserId(numericUserId, parentUserId);
                } else {
                    token = tokenService.createToken(numericUserId);
                }

                logger.info("用户 {} 目前使用token: {}", numericUserId, token);

                // 重定向到前端页面
                response.sendRedirect("/?token=" + token);
            } else {
                logger.error("获取企业微信家校用户信息失败: {}", userInfo.getString("errmsg"));
                // 重定向到错误页面
                response.sendRedirect("/login?error=user_info_failed&message=" +
                        java.net.URLEncoder.encode(userInfo.getString("errmsg"), "UTF-8"));
            }
        } catch (Exception e) {
            logger.error("处理企业微信家校授权回调时发生错误", e);
            try {
                // 重定向到错误页面
                response.sendRedirect("/login?error=internal_error&message=" +
                        java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            } catch (IOException ioException) {
                logger.error("重定向失败", ioException);
            }
        }
    }
}