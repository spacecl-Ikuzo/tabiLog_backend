package com.ikuzo.tabilog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ikuzo.tabilog.global.ApiResponse;
import com.ikuzo.tabilog.service.VerificationCodeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final VerificationCodeService verificationCodeService;

    public record EmailRequest(String email) {}
    public record VerificationRequest(String email, String code) {}

    @PostMapping("/send-verification")
    public ResponseEntity<ApiResponse<String>> sendVerification(@Valid @RequestBody EmailRequest request) {
        log.info("이메일 인증코드 전송 요청: {}", request.email());
        verificationCodeService.sendVerificationCode(request.email());
        return ResponseEntity.ok(ApiResponse.success("인증코드가 이메일로 전송되었습니다.", "SENT"));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verify(@Valid @RequestBody VerificationRequest request) {
        boolean ok = verificationCodeService.verifyCode(request.email(), request.code());
        if (ok) {
            return ResponseEntity.ok(ApiResponse.success("인증이 완료되었습니다.", "VERIFIED"));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("인증코드가 올바르지 않거나 만료되었습니다."));
    }
}


