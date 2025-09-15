package com.ikuzo.tabilog.security.jwt;

import com.ikuzo.tabilog.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 인증이 필요하지 않은 경로들은 JWT 필터를 건너뛰기
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        logger.debug("JWT Filter - Method: {}, URI: {}", method, requestURI);
        
        if (shouldSkipFilter(requestURI)) {
            logger.debug("JWT Filter - Skipping filter for URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = parseJwt(request);
            logger.debug("JWT Token parsed: {}", jwt != null ? "found" : "null");
            
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.debug("Username from JWT: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.debug("UserDetails loaded: {}", userDetails.getUsername());
                
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Authentication set successfully");
            } else {
                logger.debug("JWT validation failed or JWT is null");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
    
    private boolean shouldSkipFilter(String requestURI) {
        // 인증이 필요하지 않은 특정 경로들만 건너뛰기
        return requestURI.startsWith("/api/auth/") ||
               requestURI.startsWith("/auth/") ||
               requestURI.startsWith("/api/spots/google-search") ||
               requestURI.startsWith("/api/spots/nearby") ||
               requestURI.startsWith("/api/spots/directions") ||
               requestURI.startsWith("/api/spots/travel-time") ||
               requestURI.startsWith("/api/spots/address") ||
               requestURI.startsWith("/api/categories/regions/") ||
               requestURI.equals("/api/plans/public") ||
               requestURI.startsWith("/static/") ||
               requestURI.startsWith("/images/") ||
               requestURI.startsWith("/h2-console/") ||
               requestURI.equals("/") ||
               requestURI.equals("/health") ||
               requestURI.equals("/favicon.ico");
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
