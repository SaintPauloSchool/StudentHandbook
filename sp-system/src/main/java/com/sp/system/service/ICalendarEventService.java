package com.sp.system.service;

import com.sp.system.entity.CalendarEvent;
import java.util.List;

/**
 * 行事曆服務介面
 */
public interface ICalendarEventService {
    /**
     * 根據年月查詢事件列表
     * @param yearMonth 年月字串，格式：YYYY-MM
     * @return 事件列表
     */
    List<CalendarEvent> selectEventsByMonth(String yearMonth);
}
