package com.sp.common.core.domain;

import java.io.Serializable;

/**
 * Ztree樹結構實體類
 *
 */
public class Ztree implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 節點ID */
    private Long id;

    /** 節點父ID */
    private Long pId;

    /** 節點名稱 */
    private String name;

    /** 節點標題 */
    private String title;

    /** 是否勾選 */
    private boolean checked = false;

    /** 是否展開 */
    private boolean open = false;

    /** 是否能勾選 */
    private boolean nocheck = false;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getpId()
    {
        return pId;
    }

    public void setpId(Long pId)
    {
        this.pId = pId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public boolean isChecked()
    {
        return checked;
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }

    public boolean isOpen()
    {
        return open;
    }

    public void setOpen(boolean open)
    {
        this.open = open;
    }

    public boolean isNocheck()
    {
        return nocheck;
    }

    public void setNocheck(boolean nocheck)
    {
        this.nocheck = nocheck;
    }
}
