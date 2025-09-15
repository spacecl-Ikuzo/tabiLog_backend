package com.ikuzo.tabilog.config;

import com.ikuzo.tabilog.security.jwt.AuthEntryPointJwt;
import com.ikuzo.tabilog.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호 암호화를 위해 BCryptPasswordEncoder를 Bean으로 등록합니다.
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정을 먼저 적용 (중요: 다른 설정보다 우선)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRF 보호 비활성화 (JWT는 세션을 사용하지 않으므로)
                .csrf(csrf -> csrf.disable())
                // 인증 실패 시 처리할 핸들러 등록
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                // 세션 관리 정책을 STATELESS로 설정 (JWT 사용)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // API 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 모든 OPTIONS 요청 허용 (CORS preflight)
                        .requestMatchers("/api/auth/**").permitAll() // '/api/auth/'로 시작하는 모든 경로는 인증 없이 허용
                        .requestMatchers("/", "/health").permitAll() // 루트 경로와 헬스 체크 허용
                        .requestMatchers("/favicon.ico").permitAll() // 파비콘 허용
                        .requestMatchers("/static/**").permitAll() // 정적 리소스 허용
                        .requestMatchers("/images/**").permitAll() // 이미지 리소스 허용
                        .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 허용 (개발용)
                        .requestMatchers("/api/spots/google-search").permitAll() // Google Maps 검색 API는 인증 없이 허용
                        .requestMatchers("/api/spots/nearby").permitAll() // Google Maps 주변 검색 API는 인증 없이 허용
                        .requestMatchers("/api/spots/directions").permitAll() // Google Maps 경로 API는 인증 없이 허용
                        .requestMatchers("/api/spots/travel-time").permitAll() // 이동 시간 API 허용
                        .requestMatchers("/api/spots/address").permitAll() // 주소 변환 API 허용
                        .requestMatchers("/api/test/**").permitAll() // 테스트 API 허용 (개발용)
                        .requestMatchers("/api/plans/**").permitAll() // 플랜 API 허용 (개발용 - JWT 토큰 없이 테스트)
                        .requestMatchers("/api/categories/regions/**").permitAll() // 지역 API 허용
                        .requestMatchers("/api/expenses/**").permitAll() // Expense API는 인증 없이 허용 (개발 단계)
                        .requestMatchers("/api/plans/**").permitAll() // Plan API도 인증 없이 허용 (개발 단계)
                        .requestMatchers("/api/spots/**").permitAll() // Spot API도 인증 없이 허용 (개발 단계)
                        .requestMatchers("/api/**").permitAll() // 모든 API 경로를 인증 없이 허용 (개발 단계)
                        .anyRequest().authenticated() // 나머지 모든 요청은 인증 필요
                );

        // 직접 구현한 JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

