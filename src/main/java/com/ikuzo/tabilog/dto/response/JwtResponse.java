package com.ikuzo.tabilog.dto.response;

import lombok.Getter;

@Getter
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private String email; // 사용자 식별을 위해 email 반환
    private String userId; // 사용자 ID 반환

    public JwtResponse(String accessToken, String refreshToken, String email, String userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.userId = userId;
    }
}
