package com.sp.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sp.common.utils.http.HttpUtils;
import com.sp.system.entity.WecomSchoolDepartment;
import com.sp.system.mapper.WecomSchoolDepartmentMapper;
import com.sp.system.service.WecomSchoolDepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 企业微信学校部门业务逻辑实现类
 */
@Service
public class WecomSchoolDepartmentServiceImpl implements WecomSchoolDepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(WecomSchoolDepartmentServiceImpl.class);

    @Autowired
    private WecomSchoolDepartmentMapper wecomSchoolDepartmentMapper;

    /**
     * 获取并保存所有部门数据
     *
     * @param accessToken access_token
     * @return 部门 ID 数组
     */
    @Override
    @Transactional
    public JSONArray fetchAndSaveAllDepartments(String accessToken) {
        // 发送 GET 请求
        String url = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=" + accessToken;
        String response = HttpUtils.sendGet(url);
        // 处理结果
        JSONObject result = JSONObject.parseObject(response);
        // 如果错误码不为 0
        if (result.getInteger("errcode") == null || result.getInteger("errcode") != 0) {
            logger.error("获取部门列表失败：errcode={}, errmsg={}", 
                    result.getInteger("errcode"), result.getString("errmsg"));
            return null;
        }
        // 获取部门数组
        JSONArray departmentArray = result.getJSONArray("department");
        // 如果部门数组不为空
        if (departmentArray != null && !departmentArray.isEmpty()) {
            // 同步部门数据（增删改）
            syncDepartments(departmentArray);
        }
        // 返回部门数组
        return departmentArray;
    }

    /**
     * 查询所有学校部门
     *
     * @return 部门列表
     */
    @Override
    public List<WecomSchoolDepartment> getAllSchoolDepartments() {
        return wecomSchoolDepartmentMapper.selectAll();
    }

    /**
     * 同步部门数据（以企业微信数据为准）
     * @param wechatDepartments 企业微信部门列表
     */
    @Override
    @Transactional
    public void syncDepartments(JSONArray wechatDepartments) {
        if (CollectionUtils.isEmpty(wechatDepartments)) {
            logger.info("企业微信部门数据为空，跳过同步");
            return;
        }
        
        // 获取数据库中的所有部门
        List<WecomSchoolDepartment> dbDepartments = getAllSchoolDepartments();
        
        // 将数据库部门列表转换为 Map，方便对比
        Map<Long, WecomSchoolDepartment> dbDepartmentMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(dbDepartments)) {
            dbDepartmentMap = dbDepartments.stream()
                    .collect(Collectors.toMap(WecomSchoolDepartment::getId, dept -> dept));
        }
        
        // 标记需要删除的部门（初始为所有数据库中的部门）
        Set<Long> departmentsToDelete = new HashSet<>();
        if (!CollectionUtils.isEmpty(dbDepartments)) {
            departmentsToDelete = dbDepartments.stream()
                    .map(WecomSchoolDepartment::getId)
                    .collect(Collectors.toSet());
        }
        
        // 解析企业微信部门数据
        List<WecomSchoolDepartment> wechatDeptList = parseDepartments(wechatDepartments);
        
        // 分类处理：新增和更新
        List<WecomSchoolDepartment> departmentsToInsert = new ArrayList<>();
        List<WecomSchoolDepartment> departmentsToUpdate = new ArrayList<>();

        // 遍历企业微信部门数据
        for (WecomSchoolDepartment wechatDept : wechatDeptList) {
            // 获取部门 ID
            Long deptId = wechatDept.getId();
            
            // 从待删除列表中移除（因为这个部门在企业微信中存在）
            departmentsToDelete.remove(deptId);
            
            // 判断是新增还是更新
            if (dbDepartmentMap.containsKey(deptId)) {
                departmentsToUpdate.add(wechatDept);
            } else {
                departmentsToInsert.add(wechatDept);
            }
        }
        
        logger.info("部门数据同步 - 新增：{}, 更新：{}, 删除：{}", 
                departmentsToInsert.size(), departmentsToUpdate.size(), departmentsToDelete.size());
        
        // 执行批量操作
        executeBatchOperations(departmentsToInsert, departmentsToUpdate, departmentsToDelete);
    }

    /**
     * 解析企业微信部门数据
     * @param departmentArray 部门数组
     * @return 部门对象列表
     */
    private List<WecomSchoolDepartment> parseDepartments(JSONArray departmentArray) {
        List<WecomSchoolDepartment> departments = new ArrayList<>();
        
        if (CollectionUtils.isEmpty(departmentArray)) {
            return departments;
        }
        
        for (int i = 0; i < departmentArray.size(); i++) {
            JSONObject deptObj = departmentArray.getJSONObject(i);
            if (deptObj == null) {
                continue;
            }
            
            WecomSchoolDepartment department = new WecomSchoolDepartment();
            department.setId(deptObj.getLong("id"));
            department.setParentId(deptObj.getInteger("parentid"));
            department.setName(deptObj.getString("name"));
            department.setNameEn(deptObj.getString("name_en"));
            department.setOrder(deptObj.getInteger("order"));
            
            // 处理部门负责人列表（JSON 数组转字符串）
            JSONArray leaders = deptObj.getJSONArray("department_leader");
            if (!CollectionUtils.isEmpty(leaders)) {
                department.setDepartmentLeader(leaders.toJSONString());
            }
            
            departments.add(department);
        }
        
        return departments;
    }

    /**
     * 执行批量操作（新增、更新、删除）
     * @param toInsert 待新增的部门列表
     * @param toUpdate 待更新的部门列表
     * @param toDelete 待删除的部门 ID 集合
     */
    private void executeBatchOperations(List<WecomSchoolDepartment> toInsert, 
                                        List<WecomSchoolDepartment> toUpdate,
                                        Set<Long> toDelete) {
        // 执行批量新增
        if (!toInsert.isEmpty()) {
            wecomSchoolDepartmentMapper.batchInsertSchoolDepartments(toInsert);
            logger.debug("批量新增部门 {} 个", toInsert.size());
        }
        
        // 执行批量更新
        if (!toUpdate.isEmpty()) {
            for (WecomSchoolDepartment dept : toUpdate) {
                wecomSchoolDepartmentMapper.updateSchoolDepartment(dept);
            }
            logger.debug("批量更新部门 {} 个", toUpdate.size());
        }
        
        // 执行批量删除
        if (!toDelete.isEmpty()) {
            for (Long deptId : toDelete) {
                wecomSchoolDepartmentMapper.deleteSchoolDepartmentById(deptId);
            }
            logger.debug("批量删除部门 {} 个", toDelete.size());
        }
    }
}
