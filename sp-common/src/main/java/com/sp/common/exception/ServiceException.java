package com.sp.common.exception;

/**
 * 業務異常
 *
 */
public final class ServiceException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * 錯誤提示
     */
    private String message;

    /**
     * 錯誤明細，內部調試錯誤
     *
     */
    private String detailMessage;

    /**
     * 空構造方法，避免反序列化問題
     */
    public ServiceException()
    {
    }

    public ServiceException(String message)
    {
        this.message = message;
    }

    public String getDetailMessage()
    {
        return detailMessage;
    }

    public ServiceException setDetailMessage(String detailMessage)
    {
        this.detailMessage = detailMessage;
        return this;
    }

    @Override
    public String getMessage()
    {
        return message;
    }

    public ServiceException setMessage(String message)
    {
        this.message = message;
        return this;
    }
}