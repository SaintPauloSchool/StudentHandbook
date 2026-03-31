package com.sp.system.mapper;

import com.sp.system.entity.WecomSchoolDepartmentMember;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 企业微信学校部门成员 Mapper 接口
 */
public interface WecomSchoolDepartmentMemberMapper {

    /**
     * 批量插入学校部门成员信息（忽略重复）
     * @param members 成员列表
     */
    void batchInsertSchoolDepartmentMembers(@Param("list") List<WecomSchoolDepartmentMember> members);

    /**
     * 根据部门 ID 查询成员列表
     * @param departmentId 部门 ID
     * @return 成员列表
     */
    List<WecomSchoolDepartmentMember> selectByDepartmentId(Long departmentId);

    /**
     * 更新学校部门成员信息
     * @param member 成员信息
     */
    void updateSchoolDepartmentMember(WecomSchoolDepartmentMember member);

    /**
     * 根据 UserID 和部门 ID 删除成员
     * @param userid 用户 ID
     * @param departmentId 部门 ID
     */
    void deleteSchoolDepartmentMemberByUseridAndDepartmentId(String userid, Long departmentId);
}
