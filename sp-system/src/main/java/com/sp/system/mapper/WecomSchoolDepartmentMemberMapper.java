package com.sp.system.mapper;

import com.sp.system.entity.WecomSchoolDepartmentMember;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 企業微信學校部門成員 Mapper 接口
 */
public interface WecomSchoolDepartmentMemberMapper {

    /**
     * 批量插入學校部門成員信息（忽略重複）
     * @param members 成員列表
     */
    void batchInsertSchoolDepartmentMembers(@Param("list") List<WecomSchoolDepartmentMember> members);

    /**
     * 根據部門 ID 查詢成員列表
     * @param departmentId 部門 ID
     * @return 成員列表
     */
    List<WecomSchoolDepartmentMember> selectByDepartmentId(Long departmentId);

    /**
     * 更新學校部門成員信息
     * @param member 成員信息
     */
    void updateSchoolDepartmentMember(WecomSchoolDepartmentMember member);

    /**
     * 根據 UserID 和部門 ID 刪除成員
     * @param userid 用戶 ID
     * @param departmentId 部門 ID
     */
    void deleteSchoolDepartmentMemberByUseridAndDepartmentId(String userid, Long departmentId);
}
