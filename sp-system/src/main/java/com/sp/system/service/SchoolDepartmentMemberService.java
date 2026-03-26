package com.sp.system.service;

import com.alibaba.fastjson.JSONArray;
import com.sp.system.entity.SchoolDepartmentMember;
import java.util.List;

/**
 * 学校部门成员业务逻辑接口
 */
public interface SchoolDepartmentMemberService {

    /**
     * 批量保存学校部门成员信息
     * @param members 成员列表
     * @param departmentId 部门 ID
     */
    void batchSaveSchoolDepartmentMembers(List<SchoolDepartmentMember> members, Long departmentId);

    /**
     * 从企业微信成员列表转换并保存到指定部门
     * @param userList 成员列表（企业微信返回格式）
     * @param departmentId 部门 ID
     * @return 保存的成员数量
     */
    int convertAndSaveFromWeChat(JSONArray userList, Long departmentId);

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
    List<SchoolDepartmentMember> getMembersByDepartmentId(Long departmentId);

    /**
     * 同步部门成员数据（以企业微信数据为准）
     * @param wechatMembers 企业微信成员列表
     * @param departmentId 部门 ID
     */
    void syncMembers(JSONArray wechatMembers, Long departmentId);
}
