package com.sp.common.utils;

import com.github.pagehelper.PageHelper;
import com.sp.common.core.page.PageDomain;
import com.sp.common.core.page.TableSupport;
import com.sp.common.utils.sql.SqlUtil;

/**
 * 分頁工具類
 *
 */
public class PageUtils extends PageHelper
{
    /**
     * 設置請求分頁數據
     */
    public static void startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 清理分頁的線程變量
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }
}
