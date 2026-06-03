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
 * 學生手冊Controller
 * userId 由 TokenInterceptor 解析後注入 request attribute，Controller 直接取用
 */
@RestController
@RequestMapping("/system/handbook")
public class StudentHandbookController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(StudentHandbookController.class);

    @Autowired
    private IClassLogService classLogService;

    @Autowired
    private IParentStudentRelationService parentStudentRelationService;

    /** 從 request attribute 取得已驗證的 userId */
    private String getUserId() {
        return (String) getRequest().getAttribute(TokenInterceptor.USER_ID_ATTR);
    }

    @Log(title = "查詢過去一個月課程日誌列表", businessType = BusinessType.SELECT)
    @GetMapping("/pastMonth")
    public TableDataInfo listPastMonth(@RequestParam(required = false) String studentUserId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }
            List<ClassLog> classLogs = classLogService.getPastMonthClassLogListByParentUserId(userId, studentUserId);
            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("獲取過去一個月課程日誌列表失敗: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    @Log(title = "查詢當天課程日誌列表", businessType = BusinessType.SELECT)
    @GetMapping("/today")
    public TableDataInfo listToday(@RequestParam(required = false) String studentUserId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }
            List<ClassLog> classLogs = classLogService.getTodayClassLogListByParentUserId(userId, studentUserId);
            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("獲取當天課程日誌列表失敗: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    @Log(title = "查詢未來七天課程日誌列表（不含當天）", businessType = BusinessType.SELECT)
    @GetMapping("/nextSevenDays")
    public TableDataInfo listNextSevenDays(@RequestParam(required = false) String studentUserId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }
            List<ClassLog> classLogs = classLogService.getNextSevenDaysClassLogListByParentUserId(userId, studentUserId);
            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("獲取未來七天課程日誌列表失敗: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    @Log(title = "獲取當前家長關聯的學生列表", businessType = BusinessType.SELECT)
    @GetMapping("/students")
    public AjaxResult getRelatedStudents() {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }
            List<ParentStudentRelation> relations = parentStudentRelationService.selectByParentId(userId);
            return AjaxResult.success(relations);
        } catch (Exception e) {
            logger.error("獲取關聯學生列表失敗: {}", e.getMessage());
            return AjaxResult.error("獲取學生列表失敗: " + e.getMessage());
        }
    }

    @Log(title = "切換學生", businessType = BusinessType.UPDATE)
    @PostMapping("/switchStudent")
    public AjaxResult switchStudent(@RequestBody Map<String, String> requestBody) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }

            String studentName = requestBody.get("studentName");
            String studentUserId = requestBody.get("studentUserId");
            if (studentName == null || studentName.isEmpty()) {
                return AjaxResult.error("學生姓名不能爲空");
            }
            if (studentUserId == null || studentUserId.isEmpty()) {
                return AjaxResult.error("學生用戶ID不能爲空");
            }

            List<ParentStudentRelation> relations = parentStudentRelationService.selectByParentId(userId);
            boolean studentExists = relations.stream()
                    .anyMatch(relation -> studentName.equals(relation.getStudentName()) &&
                            studentUserId.equals(relation.getStudentUserId()));

            if (!studentExists) {
                return AjaxResult.error("家長未關聯該學生，無法切換");
            }

            return AjaxResult.success("切換學生成功", studentName);
        } catch (Exception e) {
            logger.error("切換學生失敗: {}", e.getMessage());
            return AjaxResult.error("切換學生失敗: " + e.getMessage());
        }
    }
}