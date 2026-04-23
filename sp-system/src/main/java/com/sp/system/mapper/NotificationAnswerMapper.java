package com.sp.system.mapper;

import com.sp.system.entity.NotificationAnswer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知回答Mapper接口
 */
public interface NotificationAnswerMapper {
    
    /**
     * 批量插入答案
     * @param answers 答案列表
     * @return 插入记录数
     */
    int batchInsertAnswers(@Param("answers") List<NotificationAnswer> answers);

}
