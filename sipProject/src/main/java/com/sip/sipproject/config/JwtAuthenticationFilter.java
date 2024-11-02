package com.sip.sipproject.config;

import com.sip.sipproject.bean.Customer;
import com.sip.sipproject.security.JwtTokenProvider;
import com.sip.sipproject.service.CustomerService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    //過濾器設定
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 提取 token
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Authorization")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 獲取請求的 URI
        String requestURI = request.getRequestURI();

        // 如果請求需要身份驗證
        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                // 已有token 如果使用者要求的是登入頁面，導向到頁面
                if (requestURI.equals("/sip/customer/login")) {
                    response.sendRedirect("/sip/customer/index");
                    return;  // 停止過濾鏈
                }
            } catch (JwtException e) {
            // 無效 token，重新導向至登入頁面
                response.sendRedirect("/sip/customer/login");
                return;  // 停止過濾鏈
            }
        }else if (isProtectedPath(requestURI)) {
            // 若無 token 且訪問受保護的頁面，重新導向至登入頁面
            response.sendRedirect("/sip/customer/login");
            return;
        }

        // 若無重定向需求，轉交給下一個過濾器或 API 控制器。
        filterChain.doFilter(request, response);
    }

    // 檢查路徑是否需要身份驗證
    private boolean isProtectedPath(String requestURI) {
        // 請求路徑 "/sip/customer/index"
        if (requestURI.equals("/sip/customer/index")) {
            return true;
        }

        // 請求路徑是以 "/sip/customer/index/" 開頭
        if (requestURI.startsWith("/sip/customer/index/")) {
            return true;
        }
        return false;
    }

}
