package com.sp.system.service;

import com.alibaba.fastjson.JSONArray;
import com.sp.system.entity.WecomSchoolDepartment;
import java.util.List;

/**
 * 企業微信學校部門業務邏輯接口
 */
public interface WecomSchoolDepartmentService {

    /**
     * 獲取並保存所有部門數據
     * @param accessToken access_token
     * @return 部門 ID 數組
     */
    JSONArray fetchAndSaveAllDepartments(String accessToken);

    /**
     * 查詢所有學校部門
     * @return 部門列表
     */
    List<WecomSchoolDepartment> getAllSchoolDepartments();

    /**
     * 同步部門數據（以企業微信數據為準）
     * @param wechatDepartments 企業微信部門列表
     */
    void syncDepartments(JSONArray wechatDepartments);
}
