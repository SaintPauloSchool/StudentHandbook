package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.enums.BusinessType;
import com.sp.common.exception.DuplicateSubmissionException;
import com.sp.system.entity.Notification;
import com.sp.system.entity.NotificationAnswer;
import com.sp.system.entity.vo.SubmitAnswersVO;
import com.sp.system.service.INotificationAnswerService;
import com.sp.system.service.INotificationService;
import com.sp.system.service.IParentStudentRelationService;
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
    private TokenService tokenService;

    @Autowired
    private INotificationAnswerService notificationAnswerService;

    @Autowired
    private IParentStudentRelationService parentStudentRelationService;

    /**
     * 查询已发布的通知列表
     */
    @Log(title = "查询已发布的通知列表", businessType = BusinessType.SELECT)
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            // 分页查询通知列表
            List<Notification> notifications = notificationService.selectPublishedNotificationsPage(pageNum, pageSize);
            
            // 查询总数
            int total = notificationService.countPublishedNotifications();
            
            // 返回分页数据
            Map<String, Object> result = new HashMap<>();
            result.put("list", notifications);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", (int) Math.ceil((double) total / pageSize));
            
            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("获取通知列表失败: {}", e.getMessage());
            return AjaxResult.error("获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询通知详情（包含问题列表和用户答案）
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

            // 通过Service层查询通知详情（包含问题列表）
            Map<String, Object> result = notificationService.selectNotificationDetail(notificationId);
            
            if (result == null || result.get("notification") == null) {
                return AjaxResult.error("通知不存在");
            }
            
            // 根据家长ID获取学生ID
            String studentUserId = notificationAnswerService.getStudentUserIdByParentId(parentUserId);
            
            // 查询学生对该通知的回答（只有一条记录）
            NotificationAnswer userAnswer = notificationAnswerService.getUserAnswer(notificationId, studentUserId);
            
            // 如果有回答，获取作答人信息（用answer里的user_id查询）
            if (userAnswer != null) {
                String answererInfo = parentStudentRelationService.getAnswererInfo(userAnswer.getUserId());
                result.put("userAnswer", userAnswer);
                result.put("answererInfo", answererInfo);
                result.put("hasSubmitted", true);
            } else {
                result.put("userAnswer", null);
                result.put("answererInfo", "");
                result.put("hasSubmitted", false);
            }

            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("获取通知详情失败: {}", e.getMessage());
            return AjaxResult.error("获取通知详情失败: " + e.getMessage());
        }
    }

    /**
     * 提交通知回答
     */
    @Log(title = "提交通知回答", businessType = BusinessType.INSERT)
    @PostMapping("/{notificationId}/submit")
    public AjaxResult submitAnswers(
            @PathVariable("notificationId") Long notificationId,
            @RequestBody SubmitAnswersVO submitAnswersVO) {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());

            if (parentUserId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            // 验证答案数据
            if (submitAnswersVO == null || submitAnswersVO.getAnswer() == null) {
                return AjaxResult.error("请至少回答一个问题");
            }

            String userType = "2"; // 2表示家长
            
            // 调用Service层提交答案（数据转换和保存都在Service层完成）
            int count = notificationAnswerService.submitAnswers(submitAnswersVO.getAnswer(), parentUserId, userType);
            
            logger.info("用户 {} 提交通知 {} 的回答", parentUserId, notificationId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return AjaxResult.success("提交成功", result);
        } catch (DuplicateSubmissionException e) {
            // 捕获重复提交异常，返回 409 错误码
            return AjaxResult.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("提交通知回答失败: {}", e.getMessage(), e);
            return AjaxResult.error("提交失败: " + e.getMessage());
        }
    }
}
