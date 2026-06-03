package com.sp.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security配置類
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 在實際項目中，這裡應該配置用戶認證服務
        // 由於缺少具體用戶認證邏輯，暫時留空
        // auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                // 基於token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 對於登錄和匿名接口允許訪問
                .antMatchers("/login", "/captchaImage").anonymous()
                // 靜態資源和公開接口，可匿名訪問
                .antMatchers(
                        "/",
                        "/profile/**",
                        "/system/handbook/**",
                        "/system/notice/**",
                        "/system/token/**",
                        "/system/calendar/**",
                        "/common/upload",
                        "/common/download/**",
                        "/wechat/callback/**",
                        "/wechat/oauth/callback",
                        "/tool/swagger/**",
                        "/swagger-ui/**",
                        "/dist/**",
                        "/assets/**",
                        "/sp-api/",
                        "/sp-api/**").permitAll()
                // 除上面外的所有請求全部需要鑑權認證
                .anyRequest().authenticated();

        // 添加自定義過濾器（如果需要的話）
        // http.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}