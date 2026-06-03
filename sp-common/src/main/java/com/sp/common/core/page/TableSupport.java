package com.sp.common.core.page;

import com.sp.common.core.text.Convert;
import com.sp.common.utils.ServletUtils;

/**
 * 表格數據處理
 *
 */
public class TableSupport
{
    /**
     * 當前記錄起始索引
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * 每頁顯示記錄數
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 排序列
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * 排序的方向 "desc" 或者 "asc".
     */
    public static final String IS_ASC = "isAsc";

    /**
     * 分頁參數合理化
     */
    public static final String REASONABLE = "reasonable";

    /**
     * 封裝分頁對象
     */
    public static PageDomain getPageDomain()
    {
        PageDomain pageDomain = new PageDomain();
        pageDomain.setPageNum(Convert.toInt(ServletUtils.getParameter(PAGE_NUM), 1));
        pageDomain.setPageSize(Convert.toInt(ServletUtils.getParameter(PAGE_SIZE), 10));
        pageDomain.setOrderByColumn(ServletUtils.getParameter(ORDER_BY_COLUMN));
        pageDomain.setIsAsc(ServletUtils.getParameter(IS_ASC));
        pageDomain.setReasonable(ServletUtils.getParameterToBool(REASONABLE));
        return pageDomain;
    }

    public static PageDomain buildPageRequest()
    {
        return getPageDomain();
    }
}
