package com.sp.system.entity;

/**
 * 企業微信部門實體類
 */
public class Department {
    private Long id;                      // 部門id
    private Integer parentId;             // 父親部門id
    private String name;                  // 部門名稱
    private Integer type;                 // 部門類型：1-班級, 2-年級, 3-學段, 4-校區, 5-學校
    private Integer registerYear;         // 入學年份
    private Integer standardGrade;        // 標準年級
    private Integer order;                // 排序值
    private Integer isGraduated;          // 是否畢業：1-是, 0-否
    private Integer openGroupChat;        // 是否開啓班級羣：1-是, 0-否
    private String groupChatId;           // 班級羣id


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