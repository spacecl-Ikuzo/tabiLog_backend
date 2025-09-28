package com.ikuzo.tabilog.dto.response;

import lombok.Data;

@Data
public class PasswordResetVerifyCodeResponse {
    private final String resetToken;
    private final String message;

    public PasswordResetVerifyCodeResponse(String resetToken) {
        this.resetToken = resetToken;
        this.message = "인증이 완료되었습니다.";
    }
}


