package com.sp.system.service;

import com.sp.system.entity.ClassSection;
/**
 * 課程班級Service接口
 */
public interface IClassSectionService {

    /**
     * 根據班級代碼(DSEDJ)查詢課程班級
     * 
     * @param classSectionDsedj 班級代碼(DSEDJ)
     * @return 課程班級信息
     */
    ClassSection getClassSectionByDsedj(String classSectionDsedj);

}