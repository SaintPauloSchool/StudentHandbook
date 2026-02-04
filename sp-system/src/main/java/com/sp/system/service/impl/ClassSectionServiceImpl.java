package com.sp.system.service.impl;

import com.sp.system.entity.ClassSection;
import com.sp.system.mapper.ClassSectionMapper;
import com.sp.system.service.IClassSectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * 課程班級服務實現類
 */
@Service
public class ClassSectionServiceImpl implements IClassSectionService {

    private static final Logger logger = LoggerFactory.getLogger(ClassSectionServiceImpl.class);
    
    @Autowired
    private ClassSectionMapper classSectionMapper;

    /**
     * 根據班級代碼(DSEDJ)查詢課程班級
     * 
     * @param classSectionDsedj 班級代碼(DSEDJ)
     * @return 課程班級信息
     */
    @Override
    public ClassSection getClassSectionByDsedj(String classSectionDsedj) {
        try {
            return classSectionMapper.selectClassSectionByDsedj(classSectionDsedj);
        } catch (Exception e) {
            logger.error("根據班級代碼(DSEDJ)查詢課程班級時發生異常: {}", e.getMessage(), e);
            return null;
        }
    }

}