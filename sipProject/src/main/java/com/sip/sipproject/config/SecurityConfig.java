package com.sip.sipproject.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig  {

    @Autowired
    @Lazy
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    //密碼加密
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //設定登入頁面
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 授權請求設置
                .authorizeHttpRequests(authorize -> authorize
                        // 公開訪問的路徑
                        .requestMatchers("/hotel/**","/room/**","/order/**"
                                ,"/customer/**"
                                ,"/sipIndex/**"
                                ,"/css/**","/lib/**","/js/**","/img/**","/assets/**","/jquery-ui-1.13.2/**").permitAll()
                        // 其他任何請求都必須經過身份驗證
                        .anyRequest().authenticated()
                );
        return http.build();
    }

}
