package com.sp.system.entity;

/**
 * 企业微信学校部门实体类
 */
public class WecomSchoolDepartment {
    private Long id;                      // 部门 id
    private Integer parentId;             // 父部门 id
    private String name;                  // 部门名称
    private String nameEn;                // 部门英文名称
    private Integer order;                // 在父部门中的次序值
    private String departmentLeader;      // 部门负责人的 UserID（JSON 数组字符串）

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getDepartmentLeader() {
        return departmentLeader;
    }

    public void setDepartmentLeader(String departmentLeader) {
        this.departmentLeader = departmentLeader;
    }
}
