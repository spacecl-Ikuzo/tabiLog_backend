package com.ikuzo.tabilog.security.jwt;

import com.ikuzo.tabilog.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${tabilog.app.jwtSecret}")
    private String jwtSecret;

    @Value("${tabilog.app.jwtAccessExpirationMs}")
    private int jwtExpirationMs;

    // JWT 토큰 생성
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername()) // setSubject -> subject
                .issuedAt(new Date()) // setIssuedAt -> issuedAt
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs)) // setExpiration -> expiration
                .signWith(key()) // signWith(Key, SignatureAlgorithm) -> signWith(Key)
                .compact();
    }

    // 이메일로부터 JWT 토큰 생성 (Refresh Token용)
    public String generateJwtTokenFromEmail(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // 토큰에서 사용자 이메일(username) 추출
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser() // parserBuilder() -> parser()
                .verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    // JWT 토큰 유효성 검증
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser() // parserBuilder() -> parser()
                .verifyWith(key()).build()
                .parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
