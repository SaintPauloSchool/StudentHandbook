package com.sp.system.mapper;

import com.sp.system.entity.SchoolDepartment;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 学校部门 Mapper 接口
 */
public interface SchoolDepartmentMapper {

    /**
     * 批量插入或更新学校部门信息
     * @param departments 部门列表
     */
    void batchInsertSchoolDepartments(@Param("list") List<SchoolDepartment> departments);

    /**
     * 根据 ID 查询部门信息
     * @param id 部门 ID
     * @return 部门信息
     */
    SchoolDepartment selectById(Long id);

    /**
     * 查询所有学校部门
     * @return 部门列表
     */
    List<SchoolDepartment> selectAll();

    /**
     * 更新学校部门信息
     * @param department 部门信息
     */
    void updateSchoolDepartment(SchoolDepartment department);

    /**
     * 根据 ID 删除学校部门
     * @param id 部门 ID
     */
    void deleteSchoolDepartmentById(Long id);
}
