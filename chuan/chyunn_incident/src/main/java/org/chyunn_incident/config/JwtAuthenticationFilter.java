package org.chyunn_incident.config;


import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.chyunn_incident.security.JwtTokenProvider;
import org.chyunn_incident.service.JwtBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    JwtBlacklistService jwtBlacklistService;

    //過濾器設定
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 提取 token（從 Authorization header 中獲取）
        String token = request.getHeader("Authorization");
        // 獲取請求的 URI
        String requestURI = request.getRequestURI();

        // 先檢查黑名單，如果 token 存在於 Redis 黑名單中，直接拒絕請求
        if (token != null && jwtBlacklistService.isTokenBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("Token 無效或過期");
            return; // 停止過濾鏈
        }

        // 如果請求需要身份驗證
        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                System.out.println("test1");
                if (requestURI.equals("/chyunn/login")) {
                    response.sendRedirect("/chyunn/incident/select_incident");
                    return;
                }
            } else {
                System.out.println("test2");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("Token 無效或過期");
                System.out.println("test4");
                return;
            }
        } else if (isProtectedPath(requestURI)) {
            System.out.println("test3");
            response.sendRedirect("/chyunn/login");
            return;
        }
        // 若無重定向需求，轉交給下一個過濾器或 API 控制器。
        filterChain.doFilter(request, response);
    }

    // 檢查路徑是否需要身份驗證
    private boolean isProtectedPath(String requestURI) {
        // 請求路徑 "/sip/customer/index"
        if (requestURI.equals("/chyunn/incident/")) {
            return true;
        }else if (requestURI.equals("/chyunn/asset/")){
            return true;
        }
        return false;
    }

}
