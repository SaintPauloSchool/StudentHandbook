package com.sp.system.entity.vo;

/**
 * 提交答案請求VO
 */
public class SubmitAnswersVO {

    private AnswerItemVO answer;

    private String studentId;

    public AnswerItemVO getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerItemVO answer) {
        this.answer = answer;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
