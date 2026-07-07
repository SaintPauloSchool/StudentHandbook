package com.sp.system.service.impl;

import com.sp.system.mapper.DepartmentParentBindingMapper;
import com.sp.system.service.DepartmentParentBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 部門家長綁定服務層實現類
 */
@Service
public class DepartmentParentBindingServiceImpl implements DepartmentParentBindingService {

    @Autowired
    private DepartmentParentBindingMapper departmentParentBindingMapper;

    @Override
    public boolean checkHasBoundStudents(String parentUserId) {
        return departmentParentBindingMapper.checkHasBoundStudents(parentUserId) > 0;
    }
}
