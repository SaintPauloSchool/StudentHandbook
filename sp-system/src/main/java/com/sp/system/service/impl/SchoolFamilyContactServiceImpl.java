package com.sp.system.service.impl;

import com.sp.system.mapper.SchoolFamilyContactMapper;
import com.sp.system.service.ISchoolFamilyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 家校通訊錄服務實現
 */
@Service
public class SchoolFamilyContactServiceImpl implements ISchoolFamilyContactService {

    @Autowired
    private SchoolFamilyContactMapper schoolFamilyContactMapper;

    /**
     * 判斷家長是否已綁定學生（sys_school_family_contact 有記錄）
     */
    @Override
    public boolean checkHasBoundStudents(String parentUserId) {
        return schoolFamilyContactMapper.countByParentUserId(parentUserId) > 0;
    }
}
