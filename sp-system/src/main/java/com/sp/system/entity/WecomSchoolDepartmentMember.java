package com.sp.system.entity;

/**
 * 企業微信學校部門成員實體類
 */
public class WecomSchoolDepartmentMember {
    private Long id;                    // 主鍵 ID
    private String userid;              // 成員 UserID
    private String name;                // 成員名稱
    private Long departmentId;          // 所屬部門 ID
    private String openUserid;          // 全局唯一 UserID

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getOpenUserid() {
        return openUserid;
    }

    public void setOpenUserid(String openUserid) {
        this.openUserid = openUserid;
    }
}
