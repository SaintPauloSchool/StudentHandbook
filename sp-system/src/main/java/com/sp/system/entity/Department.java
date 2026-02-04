package com.sp.system.entity;

/**
 * 企业微信部门实体类
 */
public class Department {
    private Long id;                      // 部门id
    private Integer parentId;             // 父亲部门id
    private String name;                  // 部门名称
    private Integer type;                 // 部门类型：1-班级, 2-年级, 3-学段, 4-校区, 5-学校
    private Integer registerYear;         // 入学年份
    private Integer standardGrade;        // 标准年级
    private Integer order;                // 排序值
    private Integer isGraduated;          // 是否毕业：1-是, 0-否
    private Integer openGroupChat;        // 是否开启班级群：1-是, 0-否
    private String groupChatId;           // 班级群id


    // Getter和Setter方法
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getRegisterYear() {
        return registerYear;
    }

    public void setRegisterYear(Integer registerYear) {
        this.registerYear = registerYear;
    }

    public Integer getStandardGrade() {
        return standardGrade;
    }

    public void setStandardGrade(Integer standardGrade) {
        this.standardGrade = standardGrade;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getIsGraduated() {
        return isGraduated;
    }

    public void setIsGraduated(Integer isGraduated) {
        this.isGraduated = isGraduated;
    }

    public Integer getOpenGroupChat() {
        return openGroupChat;
    }

    public void setOpenGroupChat(Integer openGroupChat) {
        this.openGroupChat = openGroupChat;
    }

    public String getGroupChatId() {
        return groupChatId;
    }

    public void setGroupChatId(String groupChatId) {
        this.groupChatId = groupChatId;
    }


}