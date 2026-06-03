package com.sp.system.mapper;

import com.sp.system.entity.DepartmentAdmin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部門管理員數據訪問層
 */
public interface DepartmentAdminMapper {

    /**
     * 批量插入部門管理員信息
     * @param admins 部門管理員列表
     */
    void batchInsertDepartmentAdmins(@Param("list") List<DepartmentAdmin> admins);

    /**
     * 根據 department_id 和 userid 查詢部門管理員是否存在
     * @param departmentId 部門ID
     * @param userid 用戶ID
     * @return 部門管理員對象，不存在則返回 null
     */
    DepartmentAdmin selectByDepartmentIdAndUserid(@Param("departmentId") Long departmentId, 
                                                    @Param("userid") String userid);

    /**
     * 根據 department_id 和 userid 更新部門管理員信息
     * @param admin 部門管理員對象
     */
    void updateByDepartmentIdAndUserid(DepartmentAdmin admin);

}
