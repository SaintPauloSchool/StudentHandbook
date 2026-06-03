package com.sp.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.sp.system.entity.DepartmentParentBinding;
import com.sp.system.mapper.DepartmentParentBindingMapper;
import com.sp.system.service.DepartmentParentBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部門家長綁定服務層實現類
 *
 */
@Service
public class DepartmentParentBindingServiceImpl implements DepartmentParentBindingService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentParentBindingServiceImpl.class);

    @Autowired
    private DepartmentParentBindingMapper departmentParentBindingMapper;

    @Override
    public List<String> getAllParentUserIds() {
        return departmentParentBindingMapper.selectAllParentUserIds();
    }
    
    @Override
    public boolean checkHasBoundStudents(String parentUserId) {
        return departmentParentBindingMapper.checkHasBoundStudents(parentUserId) > 0;
    }
    
    @Override
    public List<DepartmentParentBinding> selectByDepartmentId(Long departmentId) {
        return departmentParentBindingMapper.selectByDepartmentId(departmentId);
    }
    
    @Override
    @Transactional
    public void processParentChildren(Long departmentId, String parentUserId, JSONArray childrenArray,
                                      Map<String, DepartmentParentBinding> existingBindingMap) {
        // 處理孩子信息
        if (childrenArray != null && !childrenArray.isEmpty()) {
            // 處理家長孩子關係
            handleParentStudentRelation(
                    departmentId,
                    parentUserId,
                    childrenArray.getJSONObject(0).getString("student_userid"), //這到時候要改一下，綁定的孩子都要添加到數據庫
                    existingBindingMap
            );
        } else {
            // 沒有孩子的情況
            handleParentStudentRelation(departmentId, parentUserId, null, existingBindingMap);
        }
    }
    
    /**
     * 處理家長學生關係同步
     * @param departmentId 部門ID
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @param existingBindingMap 現有綁定記錄映射
     */
    @Transactional
    public void handleParentStudentRelation(Long departmentId, String parentUserId, String studentUserId,
                                           Map<String, DepartmentParentBinding> existingBindingMap) {
        // 從內存中的映射查找是否存在相同的記錄
        DepartmentParentBinding existingBinding = existingBindingMap.get(parentUserId);
        // 如果存在
        if (existingBinding != null) {
            // 如果存在，則更新記錄
            existingBinding.setStudentUserId(studentUserId);
            existingBinding.setUpdateTime(LocalDateTime.now());
            departmentParentBindingMapper.updateById(existingBinding);
        } else {
            // 創建或獲取部門家長綁定對象
            DepartmentParentBinding binding = this.createOrUpdateBinding(departmentId, parentUserId, studentUserId);
            // 如果不存在，則插入新記錄
            departmentParentBindingMapper.insertIgnore(binding);
        }
    }
    
    @Override
    @Transactional
    public void deleteObsoleteParentBindings(List<DepartmentParentBinding> existingBindings, 
                                           Set<String> currentParentUserIds, 
                                           Long departmentId) {
        // 記錄刪除的個數
        int deletedCount = 0;
        // 遍歷原有的綁定記錄列表，刪除不再存在的家長綁定記錄
        for (DepartmentParentBinding binding : existingBindings) {
            // 判斷當前是否存在
            if (!currentParentUserIds.contains(binding.getParentUserId())) {
                // 刪除
                boolean deleteResult = departmentParentBindingMapper.deleteById(binding.getId()) > 0;
                // 記錄刪除結果
                if (deleteResult) {
                    deletedCount++;
                }
            }
        }
        // 日誌記錄在調用方處理
        // 記錄刪除結果
        logger.info("家長數據同步完成，共刪除 {} 個家長", deletedCount);
    }
    
    /**
     * 創建或更新部門家長綁定對象
     * @param departmentId 部門ID
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID (可爲null)
     * @return 部門家長綁定對象
     */
    private DepartmentParentBinding createOrUpdateBinding(Long departmentId, String parentUserId, String studentUserId) {
        DepartmentParentBinding binding = new DepartmentParentBinding();
        binding.setDepartmentId(departmentId);
        binding.setParentUserId(parentUserId);
        if (studentUserId != null) {
            binding.setStudentUserId(studentUserId);
        }
        binding.setCreateTime(LocalDateTime.now());
        binding.setUpdateTime(LocalDateTime.now());
        return binding;
    }
}