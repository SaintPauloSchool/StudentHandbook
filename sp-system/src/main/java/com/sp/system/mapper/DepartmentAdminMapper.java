package com.sp.system.mapper;

import com.sp.system.entity.DepartmentAdmin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门管理员数据访问层
 */
public interface DepartmentAdminMapper {

    /**
     * 批量插入部门管理员信息
     * @param admins 部门管理员列表
     */
    void batchInsertDepartmentAdmins(@Param("list") List<DepartmentAdmin> admins);

    /**
     * 根据 department_id 和 userid 查询部门管理员是否存在
     * @param departmentId 部门ID
     * @param userid 用户ID
     * @return 部门管理员对象，不存在则返回 null
     */
    DepartmentAdmin selectByDepartmentIdAndUserid(@Param("departmentId") Long departmentId, 
                                                    @Param("userid") String userid);

    /**
     * 根据 department_id 和 userid 更新部门管理员信息
     * @param admin 部门管理员对象
     */
    void updateByDepartmentIdAndUserid(DepartmentAdmin admin);

}
