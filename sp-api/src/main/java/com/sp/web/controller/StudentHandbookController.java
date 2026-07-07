package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.core.page.TableDataInfo;
import com.sp.common.enums.BusinessType;
import com.sp.common.utils.ResponseUtils;
import com.sp.framework.interceptor.TokenInterceptor;
import com.sp.system.entity.ClassLog;
import com.sp.system.entity.vo.StudentRelationVO;
import com.sp.system.entity.vo.StudentPhotoVO;
import com.sp.system.service.IClassLogService;
import com.sp.system.service.IStudentPhotoService;
import com.sp.system.service.IStudentRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 學生手冊 Controller
 * <p>
 * userId 由 {@link TokenInterceptor} 解析後注入 request attribute。
 * 業務數據按家長 user_id + 學籍 student_id 查詢，studentId 為可選篩選參數。
 */
@RestController
@RequestMapping("/system/handbook")
public class StudentHandbookController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(StudentHandbookController.class);

    @Autowired
    private IClassLogService classLogService;

    @Autowired
    private IStudentRelationService studentRelationService;

    @Autowired
    private IStudentPhotoService studentPhotoService;

    /** 從 request attribute 取得已驗證的 userId */
    private String getUserId() {
        return (String) getRequest().getAttribute(TokenInterceptor.USER_ID_ATTR);
    }

    /**
     * 查詢過去一個月課程日誌（功課、測驗）
     *
     * @param studentId 學籍 student_id，可選
     */
    @Log(title = "查詢過去一個月課程日誌列表", businessType = BusinessType.SELECT)
    @GetMapping("/pastMonth")
    public TableDataInfo listPastMonth(@RequestParam(required = false) String studentId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }
            List<ClassLog> classLogs = classLogService.getPastMonthClassLogListByParentUserId(userId, studentId);
            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("獲取過去一個月課程日誌列表失敗: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    /**
     * 查詢當天課程日誌（功課、測驗）
     *
     * @param studentId 學籍 student_id，可選
     */
    @Log(title = "查詢當天課程日誌列表", businessType = BusinessType.SELECT)
    @GetMapping("/today")
    public TableDataInfo listToday(@RequestParam(required = false) String studentId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }
            List<ClassLog> classLogs = classLogService.getTodayClassLogListByParentUserId(userId, studentId);
            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("獲取當天課程日誌列表失敗: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    /**
     * 查詢未來七天課程日誌（不含當天）
     *
     * @param studentId 學籍 student_id，可選
     */
    @Log(title = "查詢未來七天課程日誌列表（不含當天）", businessType = BusinessType.SELECT)
    @GetMapping("/nextSevenDays")
    public TableDataInfo listNextSevenDays(@RequestParam(required = false) String studentId) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return ResponseUtils.createUnauthorizedResponse();
            }
            List<ClassLog> classLogs = classLogService.getNextSevenDaysClassLogListByParentUserId(userId, studentId);
            return ResponseUtils.createSuccessResponse(classLogs);
        } catch (Exception e) {
            logger.error("獲取未來七天課程日誌列表失敗: {}", e.getMessage());
            return ResponseUtils.createErrorResponse();
        }
    }

    /**
     * 獲取當前家長關聯的學生列表（含班級、親屬關係）
     */
    @Log(title = "獲取當前家長關聯的學生列表", businessType = BusinessType.SELECT)
    @GetMapping("/students")
    public AjaxResult getRelatedStudents() {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }
            List<StudentRelationVO> relations = studentRelationService.selectByParentId(userId);
            return AjaxResult.success(relations);
        } catch (Exception e) {
            logger.error("獲取關聯學生列表失敗: {}", e.getMessage());
            return AjaxResult.error("獲取學生列表失敗: " + e.getMessage());
        }
    }

    /**
     * 切換當前選中學生（僅校驗家長與學生關聯，實際狀態由前端 localStorage 維護）
     *
     * @param requestBody 請求體，需包含 studentId
     */
    @Log(title = "切換學生", businessType = BusinessType.UPDATE)
    @PostMapping("/switchStudent")
    public AjaxResult switchStudent(@RequestBody Map<String, String> requestBody) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }

            String studentId = requestBody.get("studentId");
            if (studentId == null || studentId.isEmpty()) {
                return AjaxResult.error("學生ID不能爲空");
            }

            boolean studentExists = studentRelationService.selectByParentId(userId).stream()
                    .anyMatch(relation -> studentId.equals(relation.getStudentId()));

            if (!studentExists) {
                return AjaxResult.error("家長未關聯該學生，無法切換");
            }

            return AjaxResult.success("切換學生成功");
        } catch (Exception e) {
            logger.error("切換學生失敗: {}", e.getMessage());
            return AjaxResult.error("切換學生失敗: " + e.getMessage());
        }
    }

    /**
     * 代理獲取學生照片（需驗證該學生屬於當前家長）
     * <p>
     * 未登入返回 401，無權限返回 403，照片不存在返回 404。
     */
    @Log(title = "獲取學生照片", businessType = BusinessType.SELECT)
    @GetMapping("/photo/{studentProfileNumber}")
    public void photo(@PathVariable("studentProfileNumber") String studentProfileNumber,
                      HttpServletResponse response) throws IOException {
        String userId = getUserId();
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        List<StudentRelationVO> relations = studentRelationService.selectByParentId(userId);
        boolean allowed = relations.stream()
                .anyMatch(r -> studentProfileNumber.equals(r.getStudentProfileNumber()));
        if (!allowed) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        StudentPhotoVO photoVO = studentPhotoService.fetchPhoto(studentProfileNumber);
        if (photoVO == null || photoVO.getData() == null || photoVO.getData().length == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(photoVO.getContentType());
        response.setHeader("Cache-Control", "private, max-age=3600");
        response.getOutputStream().write(photoVO.getData());
    }
}
