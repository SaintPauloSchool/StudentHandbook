package com.sp.system.service;

import com.alibaba.fastjson.JSONArray;
import com.sp.system.entity.WecomSchoolDepartmentMember;
import java.util.List;

/**
 * 企业微信学校部门成员业务逻辑接口
 */
public interface WecomSchoolDepartmentMemberService {

    /**
     * 根据部门 ID 数组获取并保存所有成员数据
     * @param departmentIdArray 部门 ID 数组
     * @param accessToken access_token
     * @return 总共获取到的成员数量
     */
    int fetchAndSaveAllMembers(JSONArray departmentIdArray, String accessToken);

    /**
     * 根据部门 ID 查询成员列表
     * @param departmentId 部门 ID
     * @return 成员列表
     */
    List<WecomSchoolDepartmentMember> getMembersByDepartmentId(Long departmentId);

    /**
     * 同步部门成员数据（以企业微信数据为准）
     * @param wechatMembers 企业微信成员列表
     * @param departmentId 部门 ID
     */
    void syncMembers(JSONArray wechatMembers, Long departmentId);
}
