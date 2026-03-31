package com.sp.system.mapper;

import com.sp.system.entity.WecomSchoolDepartment;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 企业微信学校部门 Mapper 接口
 */
public interface WecomSchoolDepartmentMapper {

    /**
     * 批量插入或更新学校部门信息
     * @param departments 部门列表
     */
    void batchInsertSchoolDepartments(@Param("list") List<WecomSchoolDepartment> departments);

    /**
     * 查询所有学校部门
     * @return 部门列表
     */
    List<WecomSchoolDepartment> selectAll();

    /**
     * 更新学校部门信息
     * @param department 部门信息
     */
    void updateSchoolDepartment(WecomSchoolDepartment department);

    /**
     * 根据 ID 删除学校部门
     * @param id 部门 ID
     */
    void deleteSchoolDepartmentById(Long id);
}
