package com.ikuzo.tabilog.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ikuzo.tabilog.security.services.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Authorization: Bearer <JWT> 를 검사하여 인증 컨텍스트를 채우는 필터.
 * - 토큰 만료: 401 + { error: "TOKEN_EXPIRED" }
 * - 기타 인증 오류: 401 + { error: "AUTH_ERROR" }
 * - 인증 불필요한 경로는 스킵( /api/auth/**, /error, /actuator 등 )
 * - CORS preflight(OPTIONS)는 스킵
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper om = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String uri = request.getRequestURI();
        final String method = request.getMethod();

        // 1) 인증 불필요한 경로 or Preflight 는 필터 스킵
        if ("OPTIONS".equalsIgnoreCase(method) || shouldSkipFilter(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2) Authorization 헤더에서 Bearer 토큰 파싱
            String jwt = parseJwt(request);

            if (StringUtils.hasText(jwt) && jwtUtils.validateJwtToken(jwt)) {
                // 3) 토큰에서 username 추출 후 인증 컨텍스트 설정
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // 4) 체인의 다음 필터로 진행
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // 만료 토큰
            log.warn("[JWT] expired: {}", e.getMessage());
            writeJsonError(response, 401, "TOKEN_EXPIRED", "로그인 세션이 만료되었습니다. 다시 로그인해주세요.");

        } catch (Exception e) {
            // 기타 인증 오류
            log.error("[JWT] auth error", e);
            writeJsonError(response, 401, "AUTH_ERROR", "인증 오류가 발생했습니다.");
        }
    }

    /** 인증 불필요 경로(permitAll)만 스킵. 절대 /api 전체를 스킵하지 말 것! */
    private boolean shouldSkipFilter(String requestURI) {
        // auth 엔드포인트(로그인/회원가입/토큰)와 오류/헬스체크/문서 경로 등
        return requestURI.startsWith("/api/auth")
                || requestURI.startsWith("/error")
                || requestURI.startsWith("/actuator")
                || requestURI.startsWith("/v3/api-docs")
                || requestURI.startsWith("/swagger-ui");
    }

    /** Authorization 헤더에서 Bearer 토큰 추출 */
    private String parseJwt(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /** 에러 응답을 JSON으로 통일 출력 */
    private void writeJsonError(HttpServletResponse res, int status, String code, String message) throws IOException {
        if (res.isCommitted()) return;
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        om.writeValue(res.getOutputStream(), Map.of(
                "status", status,
                "error", code,
                "message", message
        ));
    }
}
