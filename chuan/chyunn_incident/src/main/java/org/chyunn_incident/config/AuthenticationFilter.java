package org.chyunn_incident.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/*") // 適用於所有路徑的過濾器
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 過濾器初始化邏輯（如果需要）
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false); // 獲取當前 session，不創建新的

        // 排除不需要過濾的路徑 (例如登入頁或靜態資源)
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.startsWith("/chyunn/login") || requestURI.startsWith("/chyunn/css") || requestURI.startsWith("/chyunn/img") || requestURI.startsWith("/chyunn/js")) {
            chain.doFilter(request, response); // 繼續執行下一個過濾器或目標資源
            return;
        }

        // 驗證 session 是否有效
        if (session != null && session.getAttribute("loginID") != null) {
            // 如果 session 有效，繼續執行請求
            chain.doFilter(request, response);
        } else {
            // 如果 session 無效，重導到登入頁
            httpResponse.sendRedirect("/chyunn/login");
        }
    }

    @Override
    public void destroy() {
        // 過濾器銷毀邏輯（如果需要）
    }
}