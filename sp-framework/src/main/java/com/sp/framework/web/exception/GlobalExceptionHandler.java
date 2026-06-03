package com.sp.framework.web.exception;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import com.sp.common.core.domain.AjaxResult;
import com.sp.common.core.text.Convert;
import com.sp.common.exception.DemoModeException;
import com.sp.common.exception.ServiceException;
import com.sp.common.utils.ServletUtils;
import com.sp.common.utils.StringUtils;
import com.sp.common.utils.html.EscapeUtil;
import com.sp.common.utils.security.PermissionUtils;

/**
 * 全局異常處理器
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 權限校驗異常（ajax請求返回json，redirect請求跳轉頁面）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAuthorizationException(AccessDeniedException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("請求地址'{}',權限校驗失敗'{}'", requestURI, e.getMessage());
        if (ServletUtils.isAjaxRequest(request))
        {
            return AjaxResult.error(PermissionUtils.getMsg(e.getMessage()));
        }
        else
        {
            return new ModelAndView("error/unauth");
        }
    }

    /**
     * 請求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
            HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("請求地址'{}',不支持'{}'請求", requestURI, e.getMethod());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 攔截未知的運行時異常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("請求地址'{}',發生未知異常.", requestURI, e);
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 系統異常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("請求地址'{}',發生系統異常.", requestURI, e);
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 業務異常
     */
    @ExceptionHandler(ServiceException.class)
    public Object handleServiceException(ServiceException e, HttpServletRequest request)
    {
        log.error(e.getMessage(), e);
        if (ServletUtils.isAjaxRequest(request))
        {
            return AjaxResult.error(e.getMessage());
        }
        else
        {
            return new ModelAndView("error/service", "errorMessage", e.getMessage());
        }
    }

    /**
     * 請求路徑中缺少必需的路徑變量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public AjaxResult handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("請求路徑中缺少必需的路徑變量'{}',發生系統異常.", requestURI, e);
        return AjaxResult.error(String.format("請求路徑中缺少必需的路徑變量[%s]", e.getVariableName()));
    }

    /**
     * 請求參數類型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public AjaxResult handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
            HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        String value = Convert.toStr(e.getValue());
        if (StringUtils.isNotEmpty(value))
        {
            value = EscapeUtil.clean(value);
        }
        log.error("請求參數類型不匹配'{}',發生系統異常.", requestURI, e);
        return AjaxResult.error(String.format("請求參數類型不匹配，參數[%s]要求類型爲：'%s'，但輸入值爲：'%s'", e.getName(), e.getRequiredType().getName(), value));
    }

    /**
     * 自定義驗證異常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e)
    {
        log.error(e.getMessage(), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 演示模式異常
     */
    @ExceptionHandler(DemoModeException.class)
    public AjaxResult handleDemoModeException(DemoModeException e)
    {
        return AjaxResult.error("演示模式，不允許操作");
    }
}