package org.chyunn_incident.config;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

    @Configuration
    public class SecurityConfig {

        //密碼加密
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
        @Bean
        public FilterRegistrationBean<AuthenticationFilter> authenticationFilter() {
            FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
            registrationBean.setFilter(new AuthenticationFilter());
            registrationBean.addUrlPatterns("/*"); // 適用於所有路徑
            registrationBean.setOrder(1); // 設置過濾器執行順序
            return registrationBean;
        }


    }
