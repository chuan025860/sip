package org.chyunn_web.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 事件資料夾路徑
        registry.addResourceHandler("/uploads/incident_*/**")
//                    .addResourceLocations("file:/C:/Users/gagood72/Desktop/chuan/incident_file/");
                .addResourceLocations("file:/\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\incident_file\\");
        // Report 資料夾專用路徑
        registry.addResourceHandler("/uploads/**")
//                    .addResourceLocations("file:/C:/Users/gagood72/Desktop/chuan/incident_file/");
                .addResourceLocations("file:/\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\incident_file\\");
    }

    //設定登入頁面
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 只對特定路徑禁用 CSRF 防護
                .csrf(csrf -> csrf.disable())
                // 授權請求設置
                .authorizeHttpRequests(authorize -> authorize
                        // 公開訪問的路徑
                        .requestMatchers("/user/**","/login","/incident/**","/uploads/**","/aichat/**","/asset/**"
                                , "/css/**", "/lib/**", "/js/**", "/img/**", "/ckeditor5/**", "/jquery-ui-1.13.2/**").permitAll()
                        // 其他任何請求都必須經過身份驗證
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    //密碼加密
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public FilterRegistrationBean<AuthenticationFilter> authenticationFilter() {
//        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
//        registrationBean.setFilter(new AuthenticationFilter());
//        registrationBean.addUrlPatterns("/*"); // 適用於所有路徑
//        registrationBean.setOrder(1); // 設置過濾器執行順序
//        return registrationBean;
//    }


}
