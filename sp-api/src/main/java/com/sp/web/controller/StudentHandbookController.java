package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.core.page.TableDataInfo;
import com.sp.common.enums.BusinessType;
import com.sp.common.utils.ResponseUtils;
import com.sp.system.entity.ClassLog;
import com.sp.system.entity.ParentStudentRelation;
import com.sp.system.service.IClassLogService;
import com.sp.system.service.IParentStudentRelationService;
import com.sp.system.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学生手册Controller
 *
 */
@RestController
@RequestMapping("/system/handbook")
public class StudentHandbookController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(StudentHandbookController.class);

    @Autowired
    private IClassLogService classLogService;

    @Autowired
    private IParentStudentRelationService parentStudentRelationService;

    @Autowired
    private TokenService tokenService;

    @Log(title = "查询课程日志列表", businessType = BusinessType.SELECT)
    @GetMapping("/list")
    public TableDataInfo list() {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }

            // 通过Service获取课程日志列表
            List<ClassLog> classLogs = classLogService.getClassLogListByParentUserId(parentUserId);

            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("获取课程日志列表失败: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    @Log(title = "查询过去一个月课程日志列表", businessType = BusinessType.SELECT)
    @GetMapping("/pastMonth")
    public TableDataInfo listPastMonth() {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }

            // 通过Service获取过去一个月的课程日志列表
            List<ClassLog> classLogs = classLogService.getPastMonthClassLogListByParentUserId(parentUserId);

            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("获取过去一个月课程日志列表失败: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    @Log(title = "查询当天课程日志列表", businessType = BusinessType.SELECT)
    @GetMapping("/today")
    public TableDataInfo listToday() {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }

            // 通过Service获取当天的课程日志列表
            List<ClassLog> classLogs = classLogService.getTodayClassLogListByParentUserId(parentUserId);

            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("获取当天课程日志列表失败: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    @Log(title = "查询未来七天课程日志列表（不含当天）", businessType = BusinessType.SELECT)
    @GetMapping("/nextSevenDays")
    public TableDataInfo listNextSevenDays() {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }

            // 通过Service获取未来七天的课程日志列表
            List<ClassLog> classLogs = classLogService.getNextSevenDaysClassLogListByParentUserId(parentUserId);

            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("获取未来七天课程日志列表失败: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    @Log(title = "获取课程日志详细信息", businessType = BusinessType.SELECT)
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            // 通过Service获取课程日志详细信息
            ClassLog classLog = classLogService.getClassLogDetailByParentUserId(id, parentUserId);

            return AjaxResult.success(classLog);
        } catch (Exception e) {
            logger.error("获取课程日志详细信息失败: {}", e.getMessage());
            return AjaxResult.error("获取课程日志详细信息失败: " + e.getMessage());
        }
    }

    @Log(title = "获取当前家长关联的学生列表", businessType = BusinessType.SELECT)
    @GetMapping("/students")
    public AjaxResult getRelatedStudents() {
        try {
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
            if (parentUserId == null) {
                return AjaxResult.error("无效的访问令牌或用户未登录");
            }

            // 根据家长用户ID查询关联的学生
            List<ParentStudentRelation> relations = parentStudentRelationService.selectByParentId(parentUserId);

            // 返回完整的relations数据，前端可以从里面获取studentName
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
            // 验证token
            String parentUserId = tokenService.getParentUserIdFromRequest(getRequest());
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

            // 验证家长是否确实关联了该学生
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