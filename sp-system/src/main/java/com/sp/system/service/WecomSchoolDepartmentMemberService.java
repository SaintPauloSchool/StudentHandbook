package com.sp.system.service;

import com.alibaba.fastjson.JSONArray;
import com.sp.system.entity.WecomSchoolDepartmentMember;
import java.util.List;

/**
 * 企業微信學校部門成員業務邏輯接口
 */
public interface WecomSchoolDepartmentMemberService {

    /**
     * 根據部門 ID 數組獲取並保存所有成員數據
     * @param departmentIdArray 部門 ID 數組
     * @param accessToken access_token
     * @return 總共獲取到的成員數量
     */
    int fetchAndSaveAllMembers(JSONArray departmentIdArray, String accessToken);

    /**
     * 根據部門 ID 查詢成員列表
     * @param departmentId 部門 ID
     * @return 成員列表
     */
    List<WecomSchoolDepartmentMember> getMembersByDepartmentId(Long departmentId);

    /**
     * 同步部門成員數據（以企業微信數據為準）
     * @param wechatMembers 企業微信成員列表
     * @param departmentId 部門 ID
     */
    void syncMembers(JSONArray wechatMembers, Long departmentId);
}
