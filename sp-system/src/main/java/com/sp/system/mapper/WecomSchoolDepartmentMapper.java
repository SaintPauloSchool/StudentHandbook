package com.sp.system.mapper;

import com.sp.system.entity.WecomSchoolDepartment;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 企業微信學校部門 Mapper 接口
 */
public interface WecomSchoolDepartmentMapper {

    /**
     * 批量插入或更新學校部門信息
     * @param departments 部門列表
     */
    void batchInsertSchoolDepartments(@Param("list") List<WecomSchoolDepartment> departments);

    /**
     * 查詢所有學校部門
     * @return 部門列表
     */
    List<WecomSchoolDepartment> selectAll();

    /**
     * 更新學校部門信息
     * @param department 部門信息
     */
    void updateSchoolDepartment(WecomSchoolDepartment department);

    /**
     * 根據 ID 刪除學校部門
     * @param id 部門 ID
     */
    void deleteSchoolDepartmentById(Long id);
}
