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
        log.info("이메일 인증코드 생성 및 전송 시작: {}", email);

        // 기존 코드 제거
        verificationCodes.remove(email);

        // 이메일 서비스 통해 전송하며 코드 생성
        String code = emailService.sendVerificationCode(email);

        // 저장
        verificationCodes.put(email, new VerificationCodeInfo(code, LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)));
        log.info("인증코드 저장 완료: {} -> {} ({}분 유효)", email, code, EXPIRATION_MINUTES);
    }

    public boolean verifyCode(String email, String inputCode) {
        VerificationCodeInfo info = verificationCodes.get(email);
        if (info == null) {
            log.warn("인증코드 없음: {}", email);
            return false;
        }
        if (LocalDateTime.now().isAfter(info.expiresAt)) {
            log.warn("인증코드 만료: {}", email);
            verificationCodes.remove(email);
            return false;
        }
        boolean match = info.code.equals(inputCode);
        if (match) {
            // 일회성 사용 후 제거
            verificationCodes.remove(email);
        }
        return match;
    }

    private record VerificationCodeInfo(String code, LocalDateTime expiresAt) {}
}


