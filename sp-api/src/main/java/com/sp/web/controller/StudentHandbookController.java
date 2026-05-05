package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.core.page.TableDataInfo;
import com.sp.common.enums.BusinessType;
import com.sp.common.utils.ResponseUtils;
import com.sp.framework.interceptor.TokenInterceptor;
import com.sp.system.entity.ClassLog;
import com.sp.system.entity.ParentStudentRelation;
import com.sp.system.service.IClassLogService;
import com.sp.system.service.IParentStudentRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学生手册Controller
 * parentUserId 由 TokenInterceptor 解析後注入 request attribute，Controller 直接取用
 */
@RestController
@RequestMapping("/system/handbook")
public class StudentHandbookController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(StudentHandbookController.class);

    @Autowired
    private IClassLogService classLogService;

    @Autowired
    private IParentStudentRelationService parentStudentRelationService;

    /** 從 request attribute 取得已驗證的 parentUserId */
    private String getParentUserId() {
        return (String) getRequest().getAttribute(TokenInterceptor.PARENT_USER_ID_ATTR);
    }

    @Log(title = "查询过去一个月课程日志列表", businessType = BusinessType.SELECT)
    @GetMapping("/pastMonth")
    public TableDataInfo listPastMonth(@RequestParam(required = false) String studentUserId) {
        try {
            String parentUserId = getParentUserId();
            if (parentUserId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }
            List<ClassLog> classLogs = classLogService.getPastMonthClassLogListByParentUserId(parentUserId, studentUserId);
            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("获取过去一个月课程日志列表失败: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    @Log(title = "查询当天课程日志列表", businessType = BusinessType.SELECT)
    @GetMapping("/today")
    public TableDataInfo listToday(@RequestParam(required = false) String studentUserId) {
        try {
            String parentUserId = getParentUserId();
            if (parentUserId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }
            List<ClassLog> classLogs = classLogService.getTodayClassLogListByParentUserId(parentUserId, studentUserId);
            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("获取当天课程日志列表失败: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    @Log(title = "查询未来七天课程日志列表（不含当天）", businessType = BusinessType.SELECT)
    @GetMapping("/nextSevenDays")
    public TableDataInfo listNextSevenDays(@RequestParam(required = false) String studentUserId) {
        try {
            String parentUserId = getParentUserId();
            if (parentUserId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }
            List<ClassLog> classLogs = classLogService.getNextSevenDaysClassLogListByParentUserId(parentUserId, studentUserId);
            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("获取未来七天课程日志列表失败: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    @Log(title = "获取当前家长关联的学生列表", businessType = BusinessType.SELECT)
    @GetMapping("/students")
    public AjaxResult getRelatedStudents() {
        try {
            String parentUserId = getParentUserId();
            if (parentUserId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }
            List<ParentStudentRelation> relations = parentStudentRelationService.selectByParentId(parentUserId);
            return AjaxResult.success(relations);
        } catch (Exception e) {
            logger.error("获取关联学生列表失败: {}", e.getMessage());
            return AjaxResult.error("获取学生列表失败: " + e.getMessage());
        }
    }

    @Log(title = "切换学生", businessType = BusinessType.UPDATE)
    @PostMapping("/switchStudent")
    public AjaxResult switchStudent(@RequestBody Map<String, String> requestBody) {
        try {
            String parentUserId = getParentUserId();
            if (parentUserId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            String studentName = requestBody.get("studentName");
            String studentUserId = requestBody.get("studentUserId");
            if (studentName == null || studentName.isEmpty()) {
                return AjaxResult.error("学生姓名不能为空");
            }
            if (studentUserId == null || studentUserId.isEmpty()) {
                return AjaxResult.error("学生用户ID不能为空");
            }

            List<ParentStudentRelation> relations = parentStudentRelationService.selectByParentId(parentUserId);
            boolean studentExists = relations.stream()
                    .anyMatch(relation -> studentName.equals(relation.getStudentName()) &&
                            studentUserId.equals(relation.getStudentUserId()));

            if (!studentExists) {
                return AjaxResult.error("家长未关联该学生，无法切换");
            }

            return AjaxResult.success("切换学生成功", studentName);
        } catch (Exception e) {
            logger.error("切换学生失败: {}", e.getMessage());
            return AjaxResult.error("切换学生失败: " + e.getMessage());
        }
    }
}