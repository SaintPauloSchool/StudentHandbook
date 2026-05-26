package com.sp.web.controller;

import com.sp.common.annotation.Log;
import com.sp.common.core.controller.BaseController;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.enums.BusinessType;
import com.sp.system.entity.CalendarEvent;
import com.sp.system.service.ICalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.sp.framework.interceptor.TokenInterceptor;

/**
 * 行事曆Controller
 */
@RestController
@RequestMapping("/system/calendar")
public class CalendarEventController extends BaseController {

    @Autowired
    private ICalendarEventService calendarEventService;

    /** 從 request attribute 取得已驗證的 userId */
    private String getUserId() {
        return (String) getRequest().getAttribute(TokenInterceptor.USER_ID_ATTR);
    }

    /**
     * 查詢指定月份的行事曆事件
     */
    @Log(title = "查詢行事曆列表", businessType = BusinessType.SELECT)
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "yearMonth", required = false) String yearMonth) {
        try {
            String userId = getUserId();
            if (userId == null) {
                return AjaxResult.error("無效的訪問令牌或用戶未登錄");
            }
            List<CalendarEvent> list = calendarEventService.selectEventsByMonth(yearMonth);
            return AjaxResult.success(list);
        } catch (Exception e) {
            return AjaxResult.error("獲取行事曆列表失敗: " + e.getMessage());
        }
    }
}
