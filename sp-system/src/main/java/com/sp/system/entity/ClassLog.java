package com.sp.system.entity;

/**
 * 課程日誌實體類
 */
public class ClassLog {
    private String id;
    private String studentClass;      // 班級 (對應數據庫字段: student_class)
    private String teacher;           // 教師
    private String course;            // 課程
    private String courseType;        // 課程類型 (對應數據庫字段: course_type)
    private String content;           // 內容
    private String startDate;         // 開始日期 (對應數據庫字段: start_date)
    private String endDate;           // 結束日期 (對應數據庫字段: end_date)
    private String updateDate;        // 更新日期 (對應數據庫字段: update_date)

    // Getter和Setter方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}