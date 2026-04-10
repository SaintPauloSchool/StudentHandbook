package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.enums.BusinessType;
import com.sp.system.entity.Notification;
import com.sp.system.entity.NotificationQuestion;
import com.sp.system.mapper.NotificationQuestionMapper;
import com.sp.system.service.INotificationService;
import com.sp.system.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家校通知Controller
 */
@RestController
@RequestMapping("/system/notice")
public class ParentNoticeController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ParentNoticeController.class);

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private NotificationQuestionMapper notificationQuestionMapper;

    @Autowired
    private TokenService tokenService;

    /**
     * 查询已发布的通知列表
     */
    @Log(title = "查询已发布的通知列表", businessType = BusinessType.SELECT)
    @GetMapping("/list")
    public AjaxResult list() {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            List<Notification> notifications = notificationService.selectPublishedNotifications();
            return AjaxResult.success(notifications);
        } catch (Exception e) {
            logger.error("获取通知列表失败: {}", e.getMessage());
            return AjaxResult.error("获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询通知详情（包含问题列表）
     */
    @Log(title = "查询通知详情", businessType = BusinessType.SELECT)
    @GetMapping("/{notificationId}")
    public AjaxResult getInfo(@PathVariable("notificationId") Long notificationId) {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            // 查询通知详情
            Notification notification = notificationService.selectNotificationWithQuestions(notificationId);
            
            if (notification == null) {
                return AjaxResult.error("通知不存在");
            }

            // 查询该通知的问题列表
            List<NotificationQuestion> questions = notificationQuestionMapper.selectQuestionsByNotificationId(notificationId);

            // 组合返回通知和问题列表
            Map<String, Object> result = new HashMap<>();
            result.put("notification", notification);
            result.put("questions", questions);

            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("获取通知详情失败: {}", e.getMessage());
            return AjaxResult.error("获取通知详情失败: " + e.getMessage());
        }
    }
}
