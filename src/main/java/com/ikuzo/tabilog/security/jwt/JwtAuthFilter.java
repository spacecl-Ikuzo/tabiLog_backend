package com.ikuzo.tabilog.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - Authorization: Bearer <token> 헤더에서 토큰 추출
 * - 토큰 유효성 검사 후 SecurityContext에 Authentication 설정
 * - 예외(만료/무효)는 응답을 끝내지 않고 request attribute만 남김 → EntryPoint(AuthEntryPointJwt)가 JSON 401 생성
 * - /api/auth/**, /h2-console/**, OPTIONS 등은 필터 제외
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
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/email/")
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
            String token = resolveToken(request);

            // 이미 인증 안 되었고 토큰이 있으면 처리
            if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtils.validateJwtToken(token)) {
                    // 서버 규격: subject = userId (현재 코드 주석 기준)
                    String username = jwtUtils.getUserNameFromJwtToken(token);
                    if (StringUtils.hasText(username)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                    // 서명 불일치/형식 오류 등으로 false를 리턴한 경우 → 무효 처리 힌트만 남김
                    request.setAttribute("auth_error", "TOKEN_INVALID");
                    SecurityContextHolder.clearContext();
                    log.debug("JWT 토큰 유효성 검증 실패");
                }
            }

        } catch (ExpiredJwtException e) {
            // ⏰ 만료 토큰: EntryPoint가 JSON 401 생성
            request.setAttribute("auth_error", "TOKEN_EXPIRED");
            SecurityContextHolder.clearContext();
            log.debug("JWT expired: {}", e.getMessage());

        } catch (JwtException e) {
            // 🚫 위조/형식 오류 등
            request.setAttribute("auth_error", "TOKEN_INVALID");
            SecurityContextHolder.clearContext();
            log.debug("Invalid JWT: {}", e.getMessage());

        } catch (Exception e) {
            // 기타 예외도 무효로 처리(일관성)
            request.setAttribute("auth_error", "TOKEN_INVALID");
            SecurityContextHolder.clearContext();
            log.warn("JWT 처리 중 예외: {}", e.getMessage());
        }

        // ❗ 중요: 여기서 절대 sendError/return으로 중단하지 말고 체인을 계속
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
