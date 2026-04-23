package com.sp.system.entity.vo;

/**
 * 提交答案请求VO
 */
public class SubmitAnswersVO {
    
    /**
     * 答案数据（单个问题）
     */
    private AnswerItemVO answer;

    public AnswerItemVO getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerItemVO answer) {
        this.answer = answer;
    }
}
