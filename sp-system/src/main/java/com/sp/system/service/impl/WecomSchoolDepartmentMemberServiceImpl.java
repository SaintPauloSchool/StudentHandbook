package com.sp.system.service.impl;

import com.sp.system.mapper.WecomSchoolDepartmentMemberMapper;
import com.sp.system.service.IWecomSchoolDepartmentMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 企業微信學校部門成員服務實現
 */
@Service
public class WecomSchoolDepartmentMemberServiceImpl implements IWecomSchoolDepartmentMemberService {

    @Autowired
    private WecomSchoolDepartmentMemberMapper wecomSchoolDepartmentMemberMapper;

    @Override
    public boolean checkIsDepartmentMember(String userid) {
        if (userid == null || userid.isEmpty()) {
            return false;
        }
        return wecomSchoolDepartmentMemberMapper.countByUserid(userid) > 0;
    }
}
