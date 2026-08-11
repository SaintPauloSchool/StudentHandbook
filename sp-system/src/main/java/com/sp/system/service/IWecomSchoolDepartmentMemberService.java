package com.sp.system.service;

/**
 * 企業微信學校部門成員服務
 */
public interface IWecomSchoolDepartmentMemberService {

    /**
     * 判斷職工是否在 wecom_school_department_member 中
     */
    boolean checkIsDepartmentMember(String userid);
}
