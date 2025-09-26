package com.ikuzo.tabilog.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        final String uri = request.getRequestURI();
        final String method = request.getMethod();

        // 기본값: 무효 토큰
        String error = "TOKEN_INVALID";
        String message = "토큰이 유효하지 않습니다.";

        // JwtAuthFilter에서 심어준 힌트가 있으면 구분 (선택)
        Object hint = request.getAttribute("auth_error");
        if ("TOKEN_EXPIRED".equals(hint)) {
            error = "TOKEN_EXPIRED";
            message = "엑세스 토큰이 만료되었습니다.";
        }

        logger.warn("401 {} {} -> {} : {}", method, uri, error, authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", error);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());

        new ObjectMapper().writeValue(response.getWriter(), body);
    }
}
