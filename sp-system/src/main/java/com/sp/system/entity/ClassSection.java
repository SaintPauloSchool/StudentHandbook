package com.sp.system.entity;

/**
 * 課程班級實體類
 */
public class ClassSection {
    private Long id;
    private String classSectionDsedj;  // 班級代碼(DSEDJ)
    private String classSectionSp;     // 班級代碼(SP)

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassSectionDsedj() {
        return classSectionDsedj;
    }

    public void setClassSectionDsedj(String classSectionDsedj) {
        this.classSectionDsedj = classSectionDsedj;
    }

    public String getClassSectionSp() {
        return classSectionSp;
    }

    public void setClassSectionSp(String classSectionSp) {
        this.classSectionSp = classSectionSp;
    }
}