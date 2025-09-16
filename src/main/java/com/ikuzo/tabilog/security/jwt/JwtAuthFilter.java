package com.ikuzo.tabilog.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - Authorization: Bearer <token> 헤더에서 토큰 추출
 * - 토큰 유효성 검사 후 SecurityContext에 Authentication 설정
 * - /api/auth/**, /h2-console/** 등은 필터 제외
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        // 인증이 필요 없는 경로 (회원가입/로그인, H2 콘솔 등)
        return path.startsWith("/api/auth/")
                || path.startsWith("/h2-console")
                || path.startsWith("/actuator")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || "OPTIONS".equalsIgnoreCase(request.getMethod()); // CORS preflight
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            String token = null;

            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            }

            // 이미 인증되어 있지 않고, 토큰이 존재하면 처리
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtils.validateJwtToken(token)) {
                    String username = jwtUtils.getUserNameFromJwtToken(token); // 서버 쪽 규격: userId(아이디) 기반
                    if (username != null && !username.isBlank()) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                    log.debug("JWT 토큰 유효성 검증 실패");
                }
            }
        } catch (Exception e) {
            // 필터에서 예외를 터뜨리지 않고 다음 체인으로 넘겨서 일관된 예외 처리에 맡김
            log.error("JWT 인증 필터 처리 중 예외 발생: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
