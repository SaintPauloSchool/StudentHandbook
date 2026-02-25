package com.sp.system.service;

import com.alibaba.fastjson.JSONArray;
import com.sp.system.entity.DepartmentParentBinding;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门家长绑定服务层
 *
 */
public interface DepartmentParentBindingService {

    /**
     * 获取所有唯一的家长用户ID
     * @return 家长用户ID列表
     */
    List<String> getAllParentUserIds();
    
    /**
     * 检查家长是否绑定了学生（即绑定了学生用户ID的记录）
     * @param parentUserId 家长用户ID
     * @return 是否有绑定
     */
    boolean checkHasBoundStudents(String parentUserId);
    
    /**
     * 根据部门ID查询所有绑定记录
     * @param departmentId 部门ID
     * @return 绑定记录列表
     */
    List<DepartmentParentBinding> selectByDepartmentId(Long departmentId);

    /**
     * 处理家长的孩子信息
     *
     * @param departmentId       部门ID
     * @param parentUserId       家长用户ID
     * @param childrenArray      孩子信息数组
     * @param existingBindingMap 现有绑定记录映射
     */
    void processParentChildren(Long departmentId, String parentUserId, JSONArray childrenArray,
                               Map<String, DepartmentParentBinding> existingBindingMap);
    
    /**
     * 删除过期的家长绑定记录
     *
     * @param existingBindings     原有的绑定记录列表
     * @param currentParentUserIds 当前存在的家长用户ID集合
     * @param departmentId         部门ID
     */
    void deleteObsoleteParentBindings(List<DepartmentParentBinding> existingBindings, 
                                    Set<String> currentParentUserIds, 
                                    Long departmentId);
}