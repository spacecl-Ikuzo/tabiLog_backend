package com.ikuzo.tabilog.dto.response;

import lombok.Data;

@Data
public class PasswordResetResponse {
    
    private String message;
    
    public PasswordResetResponse() {
        this.message = "비밀번호 재설정 요청이 처리되었습니다.";
    }
    
    public PasswordResetResponse(String message) {
        this.message = message;
    }
}
