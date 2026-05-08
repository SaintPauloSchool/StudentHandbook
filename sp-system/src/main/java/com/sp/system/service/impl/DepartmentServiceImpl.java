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
 * 部门业务逻辑实现类
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
     * 批量保存部门信息
     *
     * @param departments 部门列表
     */
    @Override
    @Transactional
    public void batchSaveDepartments(List<Department> departments) {
        // 检查部门列表是否为空
        if (departments != null && !departments.isEmpty()) {
            // 批量插入或更新部门信息（根据ID判断存在与否）
            departmentMapper.batchInsertDepartments(departments);
        }
    }

    /**
     * 根据家长用户ID查询部门信息
     *
     * @param parentUserId 家长用户ID
     * @return 部门列表
     */
    @Override
    public List<Department> getDepartmentsByParentUserId(String parentUserId, String studentUserId) {
        // 查询家长用户绑定的部门ID（只返回一条）
        Long departmentId = departmentParentBindingMapper.selectByParentUserId(parentUserId, studentUserId);
        
        // 根据部门ID查询部门详细信息
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
     * 获取班级部门 ID
     *
     * @return 班级部门 ID 列表
     */
    @Override
    public List<Long> getClassDepartmentId() {
       return departmentMapper.selectClassDepartmentId();
    }

    /**
     * 根据家长ID和学生ID获取班级名
     * @param parentUserId 家长用户ID
     * @param studentUserId 学生用户ID
     * @return 班级名
     */
    @Override
    public String getClassNameByParentAndStudent(String parentUserId, String studentUserId) {
        try {
            // 1. 获取部门信息
            List<Department> departments = getDepartmentsByParentUserId(parentUserId, studentUserId);
            if (departments == null || departments.isEmpty()) {
                logger.warn("未找到家长 {} 和学生 {} 的部门信息", parentUserId, studentUserId);
                return null;
            }

            String departmentName = departments.get(0).getName();
            logger.debug("获取到部门名称: {}", departmentName);

            // 2. 使用部门名称查询 class_section 表获取 class_section_sp
            ClassSection classSection = classSectionService.getClassSectionByDsedj(departmentName);
            if (classSection == null || classSection.getClassSectionSp() == null) {
                logger.warn("未找到部门 {} 对应的班级信息", departmentName);
                return null;
            }

            return classSection.getClassSectionSp();
        } catch (Exception e) {
            logger.error("获取班级名失败: {}", e.getMessage(), e);
            return null;
        }
    }
}