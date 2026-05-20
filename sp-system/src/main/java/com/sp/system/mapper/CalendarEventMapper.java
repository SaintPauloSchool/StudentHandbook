package com.sp.system.mapper;

import com.sp.system.entity.CalendarEvent;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 行事曆 Mapper 介面
 */
public interface CalendarEventMapper {
    /**
     * 根據年月查詢事件列表
     * @param yearMonth 年月字串，格式：YYYY-MM
     * @return 事件列表
     */
    List<CalendarEvent> selectEventsByMonth(@Param("yearMonth") String yearMonth);
}
