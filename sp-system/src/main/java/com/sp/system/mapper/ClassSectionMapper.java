package com.sp.system.mapper;

import com.sp.system.entity.ClassSection;
import org.apache.ibatis.annotations.Param;

/**
 * 課程班級數據訪問層
 */
public interface ClassSectionMapper {

    /**
     * 根據班級代碼(DSEDJ)查詢課程班級
     * @param classSectionDsedj 班級代碼(DSEDJ)
     * @return 課程班級信息
     */
    ClassSection selectClassSectionByDsedj(@Param("classSectionDsedj") String classSectionDsedj);

}