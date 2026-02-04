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

    @Override
    @Transactional
    public void batchSaveDepartments(List<Department> departments) {
        if (departments != null && !departments.isEmpty()) {
            // 批量插入或更新部门信息（根据ID判断存在与否）
            departmentMapper.batchInsertDepartments(departments);
        }
    }

    @Override
    public List<Department> getDepartmentsByParentUserId(String parentUserId) {
        // 查询家长用户绑定的部门ID列表
        List<Long> departmentIds = departmentParentBindingMapper.selectByParentUserId(parentUserId);
        
        // 根据部门ID列表查询部门详细信息
        List<Department> departments = new ArrayList<>();
        for (Long departmentId : departmentIds) {
            Department department = departmentMapper.selectById(departmentId);
            if (department != null) {
                departments.add(department);
            }
        }
        
        return departments;
       
    }

    @Override
    public Long getClassDepartmentId() {
        return departmentMapper.selectClassDepartmentId();
    }
}