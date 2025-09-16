package com.ikuzo.tabilog.security.jwt;

import com.ikuzo.tabilog.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * HS256 서명 방식의 JWT 유틸.
 * - access token 에 exp(만료) 포함
 * - 만료/서명 오류 검증
 * - subject 에 username 저장
 */
@Component
public class JwtUtils {
    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    /**
     * application-*.properties(yml) 에 설정해 둘 Base64 인코딩된 256bit 시크릿
     * 예) tabilog.app.jwtSecret=YOUR_BASE64_SECRET_256BIT==
     */
    @Value("${tabilog.app.jwtSecret}")
    private String jwtSecretBase64;

    /** 액세스 토큰 만료(ms) — 예: 3600000 = 1시간 */
    @Value("${tabilog.app.jwtAccessExpirationMs}")
    private long jwtAccessExpirationMs;

    /** Base64 디코딩 후 HMAC-SHA256 키 객체 생성 */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 인증 객체로부터 JWT 생성
     * - subject: username
     * - iat(발급시각), exp(만료시각) 포함
     * - HS256 서명
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtAccessExpirationMs);

        return Jwts.builder()
                .subject(principal.getUsername())
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSigningKey(), Jwts.SIG.HS256) // HS256 서명
                .compact();
    }

    /** 토큰에서 subject(username) 추출 */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // 서명 키로 검증
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 토큰 유효성 검사
     * - 만료 시 ExpiredJwtException 그대로 던져서 필터에서 401/TOKEN_EXPIRED 로 응답하도록
     * - 그 외 파싱/서명 오류는 false 반환
     */
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] expired: {}", e.getMessage());
            throw e; // 만료는 필터에서 따로 처리할 수 있게 던진다
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[JWT] invalid: {}", e.getMessage());
            return false;
        }
    }

    /** 현재 시각 기준 액세스 토큰 만료 시각(epoch millis) 계산 유틸 (선택사항) */
    public long getAccessTokenExpiresAtEpochMillis() {
        return System.currentTimeMillis() + jwtAccessExpirationMs;
    }

    /**
     * 주어진 이메일(또는 사용자 식별자)을 JWT subject 로 사용해 토큰을 생성한다.
     * 기존 컨트롤러가 generateJwtTokenFromEmail(...)을 호출하고 있어 호환용으로 제공.
     * 주의: 실제 subject 로 email 을 쓸지 username 을 쓸지는 서비스 정책에 맞춰 통일해야 한다.
     */
    public String generateJwtTokenFromEmail(String email) {
        return buildTokenWithSubject(email);
    }

    /** 공통 토큰 생성기: subject 를 받아 iat/exp 포함하여 HS256 서명 토큰 생성 */
    private String buildTokenWithSubject(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtAccessExpirationMs);
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

}
