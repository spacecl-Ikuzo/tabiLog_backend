package com.ikuzo.tabilog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 환경별 허용 Origin 설정
        String[] origins = allowedOrigins.split(",");
        for (String origin : origins) {
            configuration.addAllowedOrigin(origin.trim());
        }
        
        // 개발 환경용 localhost 추가 (프로덕션에서도 안전하게 처리)
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("http://localhost:3001");
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedOrigin("http://localhost:5174");
        configuration.addAllowedOrigin("http://localhost:8080");
        configuration.addAllowedOrigin("http://127.0.0.1:3000");
        configuration.addAllowedOrigin("http://127.0.0.1:5173");
        configuration.addAllowedOrigin("http://127.0.0.1:5174");
        configuration.addAllowedOrigin("http://127.0.0.1:8080");

        // HTTP 메소드 설정
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("PATCH");
        configuration.addAllowedMethod("OPTIONS");
        
        // 허용할 헤더 설정
        configuration.addAllowedHeader("*");
        
        // 노출할 헤더 설정 (클라이언트가 접근 가능한 헤더)
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("Content-Type");
        configuration.addExposedHeader("X-Total-Count");
        
        // 자격 증명 허용
        configuration.setAllowCredentials(true);
        
        // preflight 요청 캐시 시간 설정 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
