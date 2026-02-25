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
 * 部门家长绑定服务层实现类
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
        // 处理孩子信息
        if (childrenArray != null && !childrenArray.isEmpty()) {
            // 处理家长孩子关系
            handleParentStudentRelation(
                    departmentId,
                    parentUserId,
                    childrenArray.getJSONObject(0).getString("student_userid"), //這到時候要改一下，綁定的孩子都要添加到數據庫
                    existingBindingMap
            );
        } else {
            // 没有孩子的情况
            handleParentStudentRelation(departmentId, parentUserId, null, existingBindingMap);
        }
    }
    
    /**
     * 处理家长学生关系同步
     * @param departmentId 部门ID
     * @param parentUserId 家长用户ID
     * @param studentUserId 学生用户ID
     * @param existingBindingMap 现有绑定记录映射
     */
    @Transactional
    public void handleParentStudentRelation(Long departmentId, String parentUserId, String studentUserId,
                                           Map<String, DepartmentParentBinding> existingBindingMap) {
        // 从内存中的映射查找是否存在相同的记录
        DepartmentParentBinding existingBinding = existingBindingMap.get(parentUserId);
        // 如果存在
        if (existingBinding != null) {
            // 如果存在，则更新记录
            existingBinding.setStudentUserId(studentUserId);
            existingBinding.setUpdateTime(LocalDateTime.now());
            departmentParentBindingMapper.updateById(existingBinding);
        } else {
            // 创建或获取部门家长绑定对象
            DepartmentParentBinding binding = this.createOrUpdateBinding(departmentId, parentUserId, studentUserId);
            // 如果不存在，则插入新记录
            departmentParentBindingMapper.insertIgnore(binding);
        }
    }
    
    @Override
    @Transactional
    public void deleteObsoleteParentBindings(List<DepartmentParentBinding> existingBindings, 
                                           Set<String> currentParentUserIds, 
                                           Long departmentId) {
        // 记录删除的个数
        int deletedCount = 0;
        // 遍历原有的绑定记录列表，删除不再存在的家长绑定记录
        for (DepartmentParentBinding binding : existingBindings) {
            // 判断当前是否存在
            if (!currentParentUserIds.contains(binding.getParentUserId())) {
                // 删除
                boolean deleteResult = departmentParentBindingMapper.deleteById(binding.getId()) > 0;
                // 记录删除结果
                if (deleteResult) {
                    deletedCount++;
                }
            }
        }
        // 日志记录在调用方处理
        // 记录删除结果
        logger.info("家长数据同步完成，共删除 {} 个家长", deletedCount);
    }
    
    /**
     * 创建或更新部门家长绑定对象
     * @param departmentId 部门ID
     * @param parentUserId 家长用户ID
     * @param studentUserId 学生用户ID (可为null)
     * @return 部门家长绑定对象
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