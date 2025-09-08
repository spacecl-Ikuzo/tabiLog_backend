package com.ikuzo.tabilog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank
    private String username; // 이메일 또는 닉네임

    @NotBlank
    private String password;
}
