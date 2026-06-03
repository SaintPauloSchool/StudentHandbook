package com.sp.common.core.domain;

import java.io.Serializable;
import java.util.List;

/**
 * CxSelect樹結構實體類
 *
 */
public class CxSelect implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 數據值字段名稱
     */
    private String v;

    /**
     * 數據標題字段名稱
     */
    private String n;

    /**
     * 子集數據字段名稱
     */
    private List<CxSelect> s;

    public CxSelect()
    {
    }

    public CxSelect(String v, String n)
    {
        this.v = v;
        this.n = n;
    }

    public List<CxSelect> getS()
    {
        return s;
    }

    public void setN(String n)
    {
        this.n = n;
    }

    public String getN()
    {
        return n;
    }

    public void setS(List<CxSelect> s)
    {
        this.s = s;
    }

    public String getV()
    {
        return v;
    }

    public void setV(String v)
    {
        this.v = v;
    }
}
