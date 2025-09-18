package com.ikuzo.tabilog.dto.response;

import lombok.Getter;

@Getter
public class SignupResponse {
    private String message;
    private String email;
    private String nickname;
    private Boolean privacyAgreement;
    private Boolean publicAgreement;
    private String redirectUrl; // 회원가입 후 이동할 URL (초대 처리용)

    public SignupResponse(String message, String email, String nickname, Boolean privacyAgreement, Boolean publicAgreement) {
        this.message = message;
        this.email = email;
        this.nickname = nickname;
        this.privacyAgreement = privacyAgreement;
        this.publicAgreement = publicAgreement;
    }

    public SignupResponse(String message, String email, String nickname, Boolean privacyAgreement, Boolean publicAgreement, String redirectUrl) {
        this.message = message;
        this.email = email;
        this.nickname = nickname;
        this.privacyAgreement = privacyAgreement;
        this.publicAgreement = publicAgreement;
        this.redirectUrl = redirectUrl;
    }
}
