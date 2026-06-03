package com.sp.system.service;

import com.alibaba.fastjson.JSONArray;
import com.sp.system.entity.DepartmentParentBinding;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部門家長綁定服務層
 *
 */
public interface DepartmentParentBindingService {

    /**
     * 獲取所有唯一的家長用戶ID
     * @return 家長用戶ID列表
     */
    List<String> getAllParentUserIds();
    
    /**
     * 檢查家長是否綁定了學生（即綁定了學生用戶ID的記錄）
     * @param parentUserId 家長用戶ID
     * @return 是否有綁定
     */
    boolean checkHasBoundStudents(String parentUserId);
    
    /**
     * 根據部門ID查詢所有綁定記錄
     * @param departmentId 部門ID
     * @return 綁定記錄列表
     */
    List<DepartmentParentBinding> selectByDepartmentId(Long departmentId);

    /**
     * 處理家長的孩子信息
     *
     * @param departmentId       部門ID
     * @param parentUserId       家長用戶ID
     * @param childrenArray      孩子信息數組
     * @param existingBindingMap 現有綁定記錄映射
     */
    void processParentChildren(Long departmentId, String parentUserId, JSONArray childrenArray,
                               Map<String, DepartmentParentBinding> existingBindingMap);
    
    /**
     * 刪除過期的家長綁定記錄
     *
     * @param existingBindings     原有的綁定記錄列表
     * @param currentParentUserIds 當前存在的家長用戶ID集合
     * @param departmentId         部門ID
     */
    void deleteObsoleteParentBindings(List<DepartmentParentBinding> existingBindings, 
                                    Set<String> currentParentUserIds, 
                                    Long departmentId);
}