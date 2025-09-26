package com.ikuzo.tabilog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final EmailService emailService;

    private static final int EXPIRATION_MINUTES = 10;

    private final Map<String, VerificationCodeInfo> verificationCodes = new ConcurrentHashMap<>();

    public void sendVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        log.info("이메일 인증코드 생성 및 전송 시작: {}", normalizedEmail);

        // 기존 코드 제거
        verificationCodes.remove(normalizedEmail);

        // 이메일 서비스 통해 전송하며 코드 생성
        String code = emailService.sendVerificationCode(normalizedEmail);

        // 저장 (정규화된 이메일 키로 저장)
        verificationCodes.put(normalizedEmail, new VerificationCodeInfo(code, LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)));
        log.info("인증코드 저장 완료: {} -> {} ({}분 유효)", normalizedEmail, code, EXPIRATION_MINUTES);
    }

    public boolean verifyCode(String email, String inputCode) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedCode = normalizeCode(inputCode);

        VerificationCodeInfo info = verificationCodes.get(normalizedEmail);
        if (info == null) {
            log.warn("인증코드 없음: {}", normalizedEmail);
            return false;
        }
        if (LocalDateTime.now().isAfter(info.expiresAt)) {
            log.warn("인증코드 만료: {}", normalizedEmail);
            verificationCodes.remove(normalizedEmail);
            return false;
        }
        boolean match = info.code.equals(normalizedCode);
        if (match) {
            // 일회성 사용 후 제거
            verificationCodes.remove(normalizedEmail);
        }
        return match;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizeCode(String code) {
        if (code == null) return null;
        String trimmed = code.trim();
        // 숫자 외 문자 제거 (하이픈/공백 등)
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        return digitsOnly;
    }

    private record VerificationCodeInfo(String code, LocalDateTime expiresAt) {}
}


