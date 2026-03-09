package com.sp.system.service.impl;

import com.sp.system.entity.Department;
import com.sp.system.mapper.DepartmentMapper;
import com.sp.system.mapper.DepartmentParentBindingMapper;
import com.sp.system.service.DepartmentService;
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

    @Autowired
    private DepartmentMapper departmentMapper;
    
    @Autowired
    private DepartmentParentBindingMapper departmentParentBindingMapper;

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
     * 获取班级部门ID
     *
     * @return 班级部门ID
     */
    @Override
    public Long getClassDepartmentId() {
        return departmentMapper.selectClassDepartmentId();
    }
}