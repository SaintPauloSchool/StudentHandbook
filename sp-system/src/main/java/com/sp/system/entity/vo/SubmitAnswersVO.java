package com.sp.system.entity.vo;

import java.util.List;

/**
 * 提交答案请求VO
 */
public class SubmitAnswersVO {
    
    /**
     * 答案列表
     */
    private List<AnswerItemVO> answers;

    public List<AnswerItemVO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerItemVO> answers) {
        this.answers = answers;
    }
}
