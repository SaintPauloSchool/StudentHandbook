package com.sp.system.mapper;

import com.sp.system.entity.vo.AttendanceRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考勤記錄 Mapper
 */
public interface AttendanceRecordMapper {

    /**
     * 按學籍 student_id 查詢考勤記錄（employee_id 關聯 student_info.student_id）
     *
     * @param accessDate 進出日期（yyyy-MM-dd），可選
     */
    List<AttendanceRecordVO> selectByStudentId(@Param("studentId") String studentId,
                                               @Param("accessDate") String accessDate,
                                               @Param("studentProfilesDatabase") String studentProfilesDatabase);
}
