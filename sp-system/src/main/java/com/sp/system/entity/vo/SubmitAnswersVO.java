package com.sp.system.entity.vo;

/**
 * 提交答案请求VO
 */
public class SubmitAnswersVO {
    
    /**
     * 答案数据（单个问题）
     */
    private AnswerItemVO answer;
    
    /**
     * 学生用户ID
     */
    private String studentUserId;

    public AnswerItemVO getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerItemVO answer) {
        this.answer = answer;
    }
    
    public String getStudentUserId() {
        return studentUserId;
    }
    
    public void setStudentUserId(String studentUserId) {
        this.studentUserId = studentUserId;
    }
}
