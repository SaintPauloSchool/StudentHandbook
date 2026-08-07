package com.sp.system.service.impl;

import com.sp.common.config.StudentProfilesProperties;
import com.sp.system.entity.vo.AttendanceRecordVO;
import com.sp.system.mapper.AttendanceRecordMapper;
import com.sp.system.service.IAttendanceRecordService;
import com.sp.system.service.IStudentRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 考勤記錄服務實現
 */
@Service
public class AttendanceRecordServiceImpl implements IAttendanceRecordService {

    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;

    @Autowired
    private IStudentRelationService studentRelationService;

    @Autowired
    private StudentProfilesProperties studentProfilesProperties;

    @Override
    public List<AttendanceRecordVO> listByParentAndStudentId(String parentUserId, String studentId, String accessDate) {
        if (!StringUtils.hasText(parentUserId) || !StringUtils.hasText(studentId)) {
            return Collections.emptyList();
        }

        boolean allowed = studentRelationService.selectByParentId(parentUserId).stream()
                .anyMatch(relation -> studentId.equals(relation.getStudentId()));
        if (!allowed) {
            return Collections.emptyList();
        }

        String dateFilter = StringUtils.hasText(accessDate) ? accessDate.trim() : null;
        List<AttendanceRecordVO> records = attendanceRecordMapper.selectByStudentId(
                studentId.trim(), dateFilter, studentProfilesProperties.getDatabase());
        return records != null ? records : Collections.emptyList();
    }
}
