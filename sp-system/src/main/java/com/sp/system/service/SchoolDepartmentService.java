package com.sp.system.service;

import com.alibaba.fastjson.JSONArray;
import com.sp.system.entity.SchoolDepartment;
import java.util.List;

/**
 * 学校部门业务逻辑接口
 */
public interface SchoolDepartmentService {

    /**
     * 批量保存学校部门信息
     * @param departments 部门列表
     */
    void batchSaveSchoolDepartments(List<SchoolDepartment> departments);

    /**
     * 从企业微信部门数组转换并保存部门数据
     * @param departmentArray 部门数组（企业微信返回格式）
     */
    void convertAndSaveFromWeChat(JSONArray departmentArray);

    /**
     * 获取并保存所有部门数据
     * @param accessToken access_token
     * @return 部门 ID 数组
     */
    JSONArray fetchAndSaveAllDepartments(String accessToken);

    /**
     * 查询所有学校部门
     * @return 部门列表
     */
    List<SchoolDepartment> getAllSchoolDepartments();

    /**
     * 同步部门数据（以企业微信数据为准）
     * @param wechatDepartments 企业微信部门列表
     */
    void syncDepartments(JSONArray wechatDepartments);
}
