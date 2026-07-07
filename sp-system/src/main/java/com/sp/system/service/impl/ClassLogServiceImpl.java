package com.sp.system.service.impl;

import com.sp.system.entity.ClassLog;
import com.sp.system.entity.ClassSection;
import com.sp.system.entity.Department;
import com.sp.system.mapper.ClassLogMapper;
import com.sp.system.service.DepartmentService;
import com.sp.system.service.IClassLogService;
import com.sp.system.service.IClassSectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 課程日誌Service業務層處理
 */
@Service
public class ClassLogServiceImpl implements IClassLogService {

    private static final Logger logger = LoggerFactory.getLogger(ClassLogServiceImpl.class);

    @Autowired
    private ClassLogMapper classLogMapper;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private IClassSectionService classSectionService;

    @Override
    public List<ClassLog> getPastMonthClassLogListByParentUserId(String parentUserId, String studentUserId) {
        try {
            String classSectionSp = getClassSectionSpByParentUserId(parentUserId, studentUserId);
            if (classSectionSp == null) {
                return Collections.emptyList();
            }

            List<ClassLog> classLogs = classLogMapper.selectPastMonthClassLogByStudentClass(classSectionSp);
            return filterValidClassLogs(classLogs);
        } catch (Exception e) {
            logger.error("根據家長用戶ID和學生用戶ID獲取過去一個月課程日誌列表失敗: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ClassLog> getTodayClassLogListByParentUserId(String parentUserId, String studentUserId) {
        try {
            String classSectionSp = getClassSectionSpByParentUserId(parentUserId, studentUserId);
            if (classSectionSp == null) {
                return Collections.emptyList();
            }

            List<ClassLog> classLogs = classLogMapper.selectTodayClassLogByStudentClass(classSectionSp);
            return filterValidClassLogs(classLogs);
        } catch (Exception e) {
            logger.error("根據家長用戶ID和學生用戶ID獲取當天課程日誌列表失敗: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ClassLog> getNextSevenDaysClassLogListByParentUserId(String parentUserId, String studentUserId) {
        try {
            String classSectionSp = getClassSectionSpByParentUserId(parentUserId, studentUserId);
            if (classSectionSp == null) {
                return Collections.emptyList();
            }

            List<ClassLog> classLogs = classLogMapper.selectNextSevenDaysClassLogByStudentClass(classSectionSp);
            return filterValidClassLogs(classLogs);
        } catch (Exception e) {
            logger.error("根據家長用戶ID和學生用戶ID獲取未來七天課程日誌列表失敗: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String getClassSectionSpByParentUserId(String parentUserId, String studentUserId) {
        try {
            List<Department> departments = departmentService.getDepartmentsByParentUserId(parentUserId, studentUserId);
            if (departments.isEmpty()) {
                logger.warn("未找到家長用戶 {} 和學生用戶 {} 對應的部門信息", parentUserId, studentUserId);
                return null;
            }

            String departmentName = departments.get(0).getName();
            logger.debug("獲取到部門名稱: {}", departmentName);

            ClassSection classSection = classSectionService.getClassSectionByDsedj(departmentName);
            if (classSection == null || classSection.getClassSectionSp() == null) {
                logger.warn("未找到部門 {} 對應的課程班級信息", departmentName);
                return null;
            }

            return classSection.getClassSectionSp();
        } catch (Exception e) {
            logger.error("根據家長用戶ID和學生用戶ID獲取班級代碼時發生異常: {}", e.getMessage(), e);
            return null;
        }
    }

    private List<ClassLog> filterValidClassLogs(List<ClassLog> classLogs) {
        if (classLogs == null) {
            return Collections.emptyList();
        }
        return classLogs;
    }
}
