package com.sp.common.exception;

/**
 * 重复提交异常
 */
public class DuplicateSubmissionException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private int code;
    
    public DuplicateSubmissionException(String message) {
        super(message);
        this.code = 409;
    }
    
    public DuplicateSubmissionException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}
