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
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 批量保存部門信息
     *
     * @param departments 部門列表
     */
    @Override
    @Transactional
    public void batchSaveDepartments(List<Department> departments) {
        // 檢查部門列表是否爲空
        if (departments != null && !departments.isEmpty()) {
            // 批量插入或更新部門信息（根據ID判斷存在與否）
            departmentMapper.batchInsertDepartments(departments);
        }
    }

    /**
     * 根據家長用戶ID查詢部門信息
     *
     * @param parentUserId 家長用戶ID
     * @return 部門列表
     */
    @Override
    public List<Department> getDepartmentsByParentUserId(String parentUserId, String studentUserId) {
        // 查詢家長用戶綁定的部門ID（只返回一條）
        Long departmentId = departmentParentBindingMapper.selectByParentUserId(parentUserId, studentUserId);
        
        // 根據部門ID查詢部門詳細信息
        List<Department> departments = new ArrayList<>();
        if (departmentId != null) {
            Department department = departmentMapper.selectById(departmentId);
            if (department != null) {
                departments.add(department);
            }
        }
        
        return departments;
       
    }

    /**
     * 獲取班級部門 ID
     *
     * @return 班級部門 ID 列表
     */
    @Override
    public List<Long> getClassDepartmentId() {
       return departmentMapper.selectClassDepartmentId();
    }

    /**
     * 根據家長ID和學生ID獲取班級名
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 班級名
     */
    @Override
    public String getClassNameByParentAndStudent(String parentUserId, String studentUserId) {
        try {
            // 1. 獲取部門信息
            List<Department> departments = getDepartmentsByParentUserId(parentUserId, studentUserId);
            if (departments == null || departments.isEmpty()) {
                logger.warn("未找到家長 {} 和學生 {} 的部門信息", parentUserId, studentUserId);
                return null;
            }

            String departmentName = departments.get(0).getName();
            logger.debug("獲取到部門名稱: {}", departmentName);

            // 2. 使用部門名稱查詢 class_section 表獲取 class_section_sp
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