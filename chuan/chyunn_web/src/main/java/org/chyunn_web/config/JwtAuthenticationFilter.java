package org.chyunn_web.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.JwtBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    JwtBlacklistService jwtBlacklistService;

    //過濾器設定
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = null;

        //  Cookie 取 token
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("authToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        // 獲取請求的 URI
        String requestURI = request.getRequestURI();

        // 先檢查黑名單，如果 token 存在於 Redis 黑名單中，直接拒絕請求
        if (token != null && jwtBlacklistService.isTokenBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("Token 無效或過期");
            return; // 停止過濾鏈
        }
        System.out.println("Token 驗證成功"+token);
        // 如果請求需要身份驗證
        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                // 取得使用者資訊
                String loginId = jwtTokenProvider.getLoginIdFromToken(token);
                List<String> roles = jwtTokenProvider.getUserRolesFromToken(token);

                // 建立權限清單
                List<GrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.replace("ROLE_", "")))
                        .collect(Collectors.toList());
                // 建立 Authentication 並放入 SecurityContext
                Authentication auth = new UsernamePasswordAuthenticationToken(loginId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                System.out.println("test1");
                if (requestURI.equals("/chyunn/login")) {
                    response.sendRedirect("/chyunn/index/index");
                    return;
                }else  if (requestURI.equals("/chyunn/loginMobile")) {
                    response.sendRedirect("/chyunn/mobile/asset/invertory_Index");
                    return;
                }
            } else {
                System.out.println("test2");
                // 清除 authToken Cookie
                Cookie cookie = new Cookie("authToken", null);
                cookie.setHttpOnly(true);
                cookie.setPath("/"); // 確保路徑正確
                cookie.setMaxAge(0); // 設定過期
                response.addCookie(cookie);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("Token 無效或過期");
                response.sendRedirect("/chyunn/login");
                return;
            }
        } else if (isProtectedPath(requestURI)) {
            System.out.println("test3 - Token 為 null 且是保護路徑");

            // 簡單用 URI 來判斷是否為手機版
            if (requestURI.startsWith("/chyunn/loginMobile")) {
                response.sendRedirect("/chyunn/loginMobile");
            } else {
                response.sendRedirect("/chyunn/login");
            }
            return;
        }
        // 若無重定向需求，轉交給下一個過濾器或 API 控制器。
        filterChain.doFilter(request, response);
    }

    private boolean isProtectedPath(String requestURI) {
        return requestURI.startsWith("/chyunn/incident/") ||
                requestURI.startsWith("/chyunn/asset/") ||
                requestURI.startsWith("/chyunn/asset_manage/") ||
                requestURI.startsWith("/chyunn/index/index") ||
                requestURI.startsWith("/chyunn/mobile/asset/");
    }

}
