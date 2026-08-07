package com.sp.system.entity.vo;

/**
 * 家長關聯學生信息（sys_student_match 通過 parent_user_id + student_user_id 關聯 sys_school_family_contact）
 */
public class StudentRelationVO {

    private String studentId;

    private String studentName;

    private String relationDesc;

    private String classSection;

    private String studentProfileNumber;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRelationDesc() {
        return relationDesc;
    }

    public void setRelationDesc(String relationDesc) {
        this.relationDesc = relationDesc;
    }

    public String getClassSection() {
        return classSection;
    }

    public void setClassSection(String classSection) {
        this.classSection = classSection;
    }

    public String getStudentProfileNumber() {
        return studentProfileNumber;
    }

    public void setStudentProfileNumber(String studentProfileNumber) {
        this.studentProfileNumber = studentProfileNumber;
    }
}
