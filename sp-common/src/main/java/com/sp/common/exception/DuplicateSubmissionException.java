package com.sp.common.exception;

/**
 * 重複提交異常
 */
public class DuplicateSubmissionException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 錯誤碼
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
