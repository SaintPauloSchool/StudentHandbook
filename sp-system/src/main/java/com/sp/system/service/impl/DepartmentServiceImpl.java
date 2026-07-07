package com.sp.system.service.impl;

import com.sp.system.entity.ClassSection;
import com.sp.system.entity.Department;
import com.sp.system.mapper.DepartmentMapper;
import com.sp.system.mapper.DepartmentParentBindingMapper;
import com.sp.system.service.DepartmentService;
import com.sp.system.service.IClassSectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 部門業務邏輯實現類
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DepartmentParentBindingMapper departmentParentBindingMapper;

    @Autowired
    private IClassSectionService classSectionService;

    @Override
    public List<Department> getDepartmentsByParentUserId(String parentUserId, String studentUserId) {
        Long departmentId = departmentParentBindingMapper.selectByParentUserId(parentUserId, studentUserId);

        List<Department> departments = new ArrayList<>();
        if (departmentId != null) {
            Department department = departmentMapper.selectById(departmentId);
            if (department != null) {
                departments.add(department);
            }
        }

        return departments;
    }

    @Override
    public String getClassNameByParentAndStudent(String parentUserId, String studentUserId) {
        try {
            List<Department> departments = getDepartmentsByParentUserId(parentUserId, studentUserId);
            if (departments == null || departments.isEmpty()) {
                logger.warn("未找到家長 {} 和學生 {} 的部門信息", parentUserId, studentUserId);
                return null;
            }

            String departmentName = departments.get(0).getName();
            logger.debug("獲取到部門名稱: {}", departmentName);

            ClassSection classSection = classSectionService.getClassSectionByDsedj(departmentName);
            if (classSection == null || classSection.getClassSectionSp() == null) {
                logger.warn("未找到部門 {} 對應的班級信息", departmentName);
                return null;
            }

            return classSection.getClassSectionSp();
        } catch (Exception e) {
            logger.error("獲取班級名失敗: {}", e.getMessage(), e);
            return null;
        }
    }
}
