package com.sp.framework.web.domain.server;

import com.sp.common.utils.Arith;

/**
 * 內存相關信息
 *
 */
public class Mem
{
    /**
     * 內存總量
     */
    private double total;

    /**
     * 已用內存
     */
    private double used;

    /**
     * 剩餘內存
     */
    private double free;

    public double getTotal()
    {
        return Arith.div(total, (1024 * 1024 * 1024), 2);
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public double getUsed()
    {
        return Arith.div(used, (1024 * 1024 * 1024), 2);
    }

    public void setUsed(long used)
    {
        this.used = used;
    }

    public double getFree()
    {
        return Arith.div(free, (1024 * 1024 * 1024), 2);
    }

    public void setFree(long free)
    {
        this.free = free;
    }

    public double getUsage()
    {
        return Arith.mul(Arith.div(used, total, 4), 100);
    }
}
