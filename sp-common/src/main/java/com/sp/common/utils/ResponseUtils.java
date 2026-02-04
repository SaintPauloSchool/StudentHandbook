package com.sp.common.utils;

import com.sp.common.core.page.TableDataInfo;
import java.util.Collections;
import java.util.List;

/**
 * 响应工具类，用于创建统一格式的响应数据
 * 
 * @author author
 */
public class ResponseUtils {
    
    /**
     * 创建成功响应
     * @param data 响应数据列表
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
     * 创建未授权响应
     * @return TableDataInfo
     */
    public static TableDataInfo createUnauthorizedResponse() {
        TableDataInfo dataTable = new TableDataInfo();
        dataTable.setCode(401); // 未授权
        dataTable.setRows(Collections.emptyList());
        dataTable.setTotal(0);
        return dataTable;
    }
    
    /**
     * 创建错误响应
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
     * 创建自定义状态码的响应
     * @param code 状态码
     * @param data 响应数据
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
     * 创建带消息的响应
     * @param code 状态码
     * @param data 响应数据
     * @param msg 响应消息
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