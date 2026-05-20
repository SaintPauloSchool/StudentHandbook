package com.sp.system.service.impl;

import com.sp.system.entity.CalendarEvent;
import com.sp.system.mapper.CalendarEventMapper;
import com.sp.system.service.ICalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 行事曆服務實作
 */
@Service
public class CalendarEventServiceImpl implements ICalendarEventService {

    @Autowired
    private CalendarEventMapper calendarEventMapper;

    @Override
    public List<CalendarEvent> selectEventsByMonth(String yearMonth) {
        return calendarEventMapper.selectEventsByMonth(yearMonth);
    }
}
