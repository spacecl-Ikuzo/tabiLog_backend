package com.ikuzo.tabilog.dto.response;

import lombok.Getter;

@Getter
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private String email; // 사용자 식별을 위해 email 반환
    private String userId; // 사용자 ID 반환
    private String nickname; // 닉네임 반환
    private String redirectUrl; // 로그인 후 이동할 URL (초대 처리용)

    public JwtResponse(String accessToken, String refreshToken, String email, String userId, String nickname) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.userId = userId;
        this.nickname = nickname;
    }

    public JwtResponse(String accessToken, String refreshToken, String email, String userId, String nickname, String redirectUrl) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.userId = userId;
        this.nickname = nickname;
        this.redirectUrl = redirectUrl;
    }
}
