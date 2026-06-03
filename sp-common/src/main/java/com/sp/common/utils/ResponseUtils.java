package com.sp.common.utils;

import com.sp.common.core.page.TableDataInfo;
import java.util.Collections;
import java.util.List;

/**
 * 響應工具類，用於創建統一格式的響應數據
 * 
 * @author author
 */
public class ResponseUtils {
    
    /**
     * 創建成功響應
     * @param data 響應數據列表
     * @return TableDataInfo
     */
    public static <T> TableDataInfo createSuccessResponse(List<T> data) {
        TableDataInfo dataTable = new TableDataInfo();
        dataTable.setCode(200);
        dataTable.setRows(data);
        dataTable.setTotal(data != null ? data.size() : 0);
        return dataTable;
    }
    
    /**
     * 創建未授權響應
     * @return TableDataInfo
     */
    public static TableDataInfo createUnauthorizedResponse() {
        TableDataInfo dataTable = new TableDataInfo();
        dataTable.setCode(401); // 未授權
        dataTable.setRows(Collections.emptyList());
        dataTable.setTotal(0);
        return dataTable;
    }
    
    /**
     * 創建錯誤響應
     * @return TableDataInfo
     */
    public static TableDataInfo createErrorResponse() {
        TableDataInfo dataTable = new TableDataInfo();
        dataTable.setCode(200);
        dataTable.setRows(Collections.emptyList());
        dataTable.setTotal(0);
        return dataTable;
    }
    
    /**
     * 創建自定義狀態碼的響應
     * @param code 狀態碼
     * @param data 響應數據
     * @return TableDataInfo
     */
    public static <T> TableDataInfo createCustomResponse(int code, List<T> data) {
        TableDataInfo dataTable = new TableDataInfo();
        dataTable.setCode(code);
        dataTable.setRows(data);
        dataTable.setTotal(data != null ? data.size() : 0);
        return dataTable;
    }
    
    /**
     * 創建帶消息的響應
     * @param code 狀態碼
     * @param data 響應數據
     * @param msg 響應消息
     * @return TableDataInfo
     */
    public static <T> TableDataInfo createResponseWithMessage(int code, List<T> data, String msg) {
        TableDataInfo dataTable = new TableDataInfo();
        dataTable.setCode(code);
        dataTable.setRows(data);
        dataTable.setTotal(data != null ? data.size() : 0);
        dataTable.setMsg(msg);
        return dataTable;
    }
}