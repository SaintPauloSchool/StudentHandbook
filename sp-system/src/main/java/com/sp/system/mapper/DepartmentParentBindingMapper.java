package com.sp.system.mapper;

import com.sp.system.entity.DepartmentParentBinding;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门家长绑定数据层
 *
 */
public interface DepartmentParentBindingMapper {

    /**
     * 根据家长ID和学生ID查询绑定的部门
     * @param parentUserId 家长用户ID
     * @param studentUserId 学生用户ID
     * @return 部门ID（只返回一条）
     */
    Long selectByParentUserId(@Param("parentUserId") String parentUserId, @Param("studentUserId") String studentUserId);

    /**
     * 安全插入部门家长绑定记录（忽略重复）
     * @param binding 部门家长绑定对象
     * @return 影响的行数
     */
    int insertIgnore(DepartmentParentBinding binding);

    /**
     * 查询所有唯一的家长用户ID
     * @return 家长用户ID列表
     */
    List<String> selectAllParentUserIds();
    
    /**
     * 检查家长是否绑定了学生（即绑定了学生用户ID的记录）
     * @param parentUserId 家长用户ID
     * @return 记录数量
     */
    int checkHasBoundStudents(@Param("parentUserId") String parentUserId);
    
    /**
     * 根据部门ID查询所有绑定记录
     * @param departmentId 部门ID
     * @return 绑定记录列表
     */
    List<DepartmentParentBinding> selectByDepartmentId(@Param("departmentId") Long departmentId);
    
    /**
     * 根据ID更新绑定记录
     * @param binding 绑定记录
     * @return 影响的行数
     */
    int updateById(DepartmentParentBinding binding);
    
    /**
     * 根据ID删除绑定记录
     * @param id 记录ID
     * @return 影响的行数
     */
    int deleteById(@Param("id") Long id);
}