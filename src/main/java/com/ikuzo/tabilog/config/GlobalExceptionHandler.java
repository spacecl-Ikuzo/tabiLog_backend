package com.ikuzo.tabilog.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 📌 공통 에러 응답 객체
    //   - status : HTTP 상태 코드
    //   - error  : 에러 코드 문자열
    //   - message: 사용자에게 보여줄 메시지 (일본어)
    //   - details: 필드별 상세 메시지 (일본어)
    static class ErrorResponse {
        public int status;
        public String error;
        public String message;
        public Map<String, Object> details;

        public ErrorResponse(int status, String error, String message, Map<String, Object> details) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.details = details;
        }
    }

    // ✅ DB 유니크 제약(중복 가입 등) 위반 시 처리
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict
    public ErrorResponse handleDuplicate(DataIntegrityViolationException ex) {
        // DB에서 던져주는 실제 에러 메시지 (제약명 포함)
        String rootMsg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";

        Map<String, Object> details = new HashMap<>();

        // 📌 제약명으로 어떤 컬럼이 중복됐는지 구분
        //   → 사용자에게는 일본어 메시지를 반환
        if (rootMsg.contains("uk_user_userid")) {
            details.put("userId", "すでに使用されているIDです。"); // 사용자: 일본어 / 개발자 주석: 아이디 중복
        }
        if (rootMsg.contains("uk_user_email")) {
            details.put("email", "すでに使用されているメールアドレスです。"); // 이메일 중복
        }
        if (rootMsg.contains("uk_user_nickname")) {
            details.put("nickname", "すでに使用されているニックネームです。"); // 닉네임 중복
        }

        return new ErrorResponse(
                409,
                "DUPLICATE",
                "重複のため登録できませんでした。", // 전체 메시지 (사용자에게 보임, 일본어)
                details
        );
    }
}
