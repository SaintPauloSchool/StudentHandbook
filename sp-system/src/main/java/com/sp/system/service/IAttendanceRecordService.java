package com.sp.system.service;

import com.sp.system.entity.vo.AttendanceRecordVO;

import java.util.List;

/**
 * 考勤記錄服務
 */
public interface IAttendanceRecordService {

    /**
     * 查詢家長關聯學生的考勤記錄
     *
     * @param parentUserId 家長 user_id
     * @param studentId    學籍 student_id
     * @param accessDate   進出日期（yyyy-MM-dd），可選
     */
    List<AttendanceRecordVO> listByParentAndStudentId(String parentUserId, String studentId, String accessDate);
}
