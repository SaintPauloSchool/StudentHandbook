package com.sp.system.mapper;

import com.sp.system.entity.DepartmentParentBinding;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部門家長綁定數據層
 *
 */
public interface DepartmentParentBindingMapper {

    /**
     * 根據家長ID和學生ID查詢綁定的部門
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 部門ID（只返回一條）
     */
    Long selectByParentUserId(@Param("parentUserId") String parentUserId, @Param("studentUserId") String studentUserId);

    /**
     * 安全插入部門家長綁定記錄（忽略重複）
     * @param binding 部門家長綁定對象
     * @return 影響的行數
     */
    int insertIgnore(DepartmentParentBinding binding);

    /**
     * 查詢所有唯一的家長用戶ID
     * @return 家長用戶ID列表
     */
    List<String> selectAllParentUserIds();
    
    /**
     * 檢查家長是否綁定了學生（即綁定了學生用戶ID的記錄）
     * @param parentUserId 家長用戶ID
     * @return 記錄數量
     */
    int checkHasBoundStudents(@Param("parentUserId") String parentUserId);
    
    /**
     * 根據部門ID查詢所有綁定記錄
     * @param departmentId 部門ID
     * @return 綁定記錄列表
     */
    List<DepartmentParentBinding> selectByDepartmentId(@Param("departmentId") Long departmentId);
    
    /**
     * 根據ID更新綁定記錄
     * @param binding 綁定記錄
     * @return 影響的行數
     */
    int updateById(DepartmentParentBinding binding);
    
    /**
     * 根據ID刪除綁定記錄
     * @param id 記錄ID
     * @return 影響的行數
     */
    int deleteById(@Param("id") Long id);
}