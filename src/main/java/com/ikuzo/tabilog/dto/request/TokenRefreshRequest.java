package com.ikuzo.tabilog.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class TokenRefreshRequest {
    
    @NotBlank(message = "Refresh token은 필수입니다")
    private String refreshToken;
}