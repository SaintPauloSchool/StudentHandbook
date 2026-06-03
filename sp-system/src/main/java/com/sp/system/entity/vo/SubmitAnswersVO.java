package com.sp.system.entity.vo;

/**
 * 提交答案請求VO
 */
public class SubmitAnswersVO {
    
    /**
     * 答案數據（單個問題）
     */
    private AnswerItemVO answer;
    
    /**
     * 學生用戶ID
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
