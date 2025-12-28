package com.example.shop.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //비활성화
        http.csrf((auth)->auth.disable());
        http.formLogin((auth)->auth.disable());
        http.httpBasic((auth)->auth.disable());

        // H2 콘솔 iframe 허용
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        //경로별 인가
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/member/sign-up", "/", "/member/login","/h2-console/**").permitAll() //모두 허용
                        .requestMatchers("/admin").hasRole("ADMIN") //admin 만
                        .anyRequest().authenticated()); //인증된 사용자만

        //세션 stateless 로
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
