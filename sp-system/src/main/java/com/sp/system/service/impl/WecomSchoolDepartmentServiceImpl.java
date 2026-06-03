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
 * 企業微信學校部門業務邏輯實現類
 */
@Service
public class WecomSchoolDepartmentServiceImpl implements WecomSchoolDepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(WecomSchoolDepartmentServiceImpl.class);

    @Autowired
    private WecomSchoolDepartmentMapper wecomSchoolDepartmentMapper;

    /**
     * 獲取並保存所有部門數據
     *
     * @param accessToken access_token
     * @return 部門 ID 數組
     */
    @Override
    @Transactional
    public JSONArray fetchAndSaveAllDepartments(String accessToken) {
        // 發送 GET 請求
        String url = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=" + accessToken;
        String response = HttpUtils.sendGet(url);
        // 處理結果
        JSONObject result = JSONObject.parseObject(response);
        // 如果錯誤碼不爲 0
        if (result.getInteger("errcode") == null || result.getInteger("errcode") != 0) {
            logger.error("獲取部門列表失敗：errcode={}, errmsg={}", 
                    result.getInteger("errcode"), result.getString("errmsg"));
            return null;
        }
        // 獲取部門數組
        JSONArray departmentArray = result.getJSONArray("department");
        // 如果部門數組不爲空
        if (departmentArray != null && !departmentArray.isEmpty()) {
            // 同步部門數據（增刪改）
            syncDepartments(departmentArray);
        }
        // 返回部門數組
        return departmentArray;
    }

    /**
     * 查詢所有學校部門
     *
     * @return 部門列表
     */
    @Override
    public List<WecomSchoolDepartment> getAllSchoolDepartments() {
        return wecomSchoolDepartmentMapper.selectAll();
    }

    /**
     * 同步部門數據（以企業微信數據為準）
     * @param wechatDepartments 企業微信部門列表
     */
    @Override
    @Transactional
    public void syncDepartments(JSONArray wechatDepartments) {
        if (CollectionUtils.isEmpty(wechatDepartments)) {
            logger.info("企業微信部門數據爲空，跳過同步");
            return;
        }
        
        // 獲取數據庫中的所有部門
        List<WecomSchoolDepartment> dbDepartments = getAllSchoolDepartments();
        
        // 將數據庫部門列錶轉換爲 Map，方便對比
        Map<Long, WecomSchoolDepartment> dbDepartmentMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(dbDepartments)) {
            dbDepartmentMap = dbDepartments.stream()
                    .collect(Collectors.toMap(WecomSchoolDepartment::getId, dept -> dept));
        }
        
        // 標記需要刪除的部門（初始爲所有數據庫中的部門）
        Set<Long> departmentsToDelete = new HashSet<>();
        if (!CollectionUtils.isEmpty(dbDepartments)) {
            departmentsToDelete = dbDepartments.stream()
                    .map(WecomSchoolDepartment::getId)
                    .collect(Collectors.toSet());
        }
        
        // 解析企業微信部門數據
        List<WecomSchoolDepartment> wechatDeptList = parseDepartments(wechatDepartments);
        
        // 分類處理：新增和更新
        List<WecomSchoolDepartment> departmentsToInsert = new ArrayList<>();
        List<WecomSchoolDepartment> departmentsToUpdate = new ArrayList<>();

        // 遍歷企業微信部門數據
        for (WecomSchoolDepartment wechatDept : wechatDeptList) {
            // 獲取部門 ID
            Long deptId = wechatDept.getId();
            
            // 從待刪除列表中移除（因爲這個部門在企業微信中存在）
            departmentsToDelete.remove(deptId);
            
            // 判斷是新增還是更新
            if (dbDepartmentMap.containsKey(deptId)) {
                departmentsToUpdate.add(wechatDept);
            } else {
                departmentsToInsert.add(wechatDept);
            }
        }
        
        logger.info("部門數據同步 - 新增：{}, 更新：{}, 刪除：{}", 
                departmentsToInsert.size(), departmentsToUpdate.size(), departmentsToDelete.size());
        
        // 執行批量操作
        executeBatchOperations(departmentsToInsert, departmentsToUpdate, departmentsToDelete);
    }

    /**
     * 解析企業微信部門數據
     * @param departmentArray 部門數組
     * @return 部門對象列表
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
            
            // 處理部門負責人列表（JSON 數組轉字符串）
            JSONArray leaders = deptObj.getJSONArray("department_leader");
            if (!CollectionUtils.isEmpty(leaders)) {
                department.setDepartmentLeader(leaders.toJSONString());
            }
            
            departments.add(department);
        }
        
        return departments;
    }

    /**
     * 執行批量操作（新增、更新、刪除）
     * @param toInsert 待新增的部門列表
     * @param toUpdate 待更新的部門列表
     * @param toDelete 待刪除的部門 ID 集合
     */
    private void executeBatchOperations(List<WecomSchoolDepartment> toInsert, 
                                        List<WecomSchoolDepartment> toUpdate,
                                        Set<Long> toDelete) {
        // 執行批量新增
        if (!toInsert.isEmpty()) {
            wecomSchoolDepartmentMapper.batchInsertSchoolDepartments(toInsert);
            logger.debug("批量新增部門 {} 個", toInsert.size());
        }
        
        // 執行批量更新
        if (!toUpdate.isEmpty()) {
            for (WecomSchoolDepartment dept : toUpdate) {
                wecomSchoolDepartmentMapper.updateSchoolDepartment(dept);
            }
            logger.debug("批量更新部門 {} 個", toUpdate.size());
        }
        
        // 執行批量刪除
        if (!toDelete.isEmpty()) {
            for (Long deptId : toDelete) {
                wecomSchoolDepartmentMapper.deleteSchoolDepartmentById(deptId);
            }
            logger.debug("批量刪除部門 {} 個", toDelete.size());
        }
    }
}
