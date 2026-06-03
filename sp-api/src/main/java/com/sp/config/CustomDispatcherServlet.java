package com.sp.config;

import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomDispatcherServlet extends DispatcherServlet {

    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 在請求處理前設置屬性，防止錯誤頁面處理
        request.setAttribute("javax.servlet.error.status_code", null);
        request.setAttribute("javax.servlet.error.message", null);
        request.setAttribute("javax.servlet.error.exception", null);
        request.setAttribute("javax.servlet.error.request_uri", null);
        request.setAttribute("javax.servlet.error.servlet_name", null);

        super.doService(request, response);
    }

    @Override
    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 對於找不到處理器的情況，直接返回200狀態碼並轉發到index.html
        // 這樣可以避免觸發錯誤頁面處理邏輯
        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_OK);
            request.getRequestDispatcher("/dist/index.html").forward(request, response);
        }
    }
}