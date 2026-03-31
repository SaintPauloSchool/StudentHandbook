package com.sp.system.entity;

/**
 * 企业微信学校部门成员实体类
 */
public class WecomSchoolDepartmentMember {
    private Long id;                    // 主键 ID
    private String userid;              // 成员 UserID
    private String name;                // 成员名称
    private Long departmentId;          // 所属部门 ID
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
