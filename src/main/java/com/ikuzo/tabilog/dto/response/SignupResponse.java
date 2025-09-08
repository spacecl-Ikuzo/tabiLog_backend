package com.ikuzo.tabilog.dto.response;

import lombok.Getter;

@Getter
public class SignupResponse {
    private String message;
    private String email;
    private String nickname;
    private Boolean privacyAgreement;
    private Boolean publicAgreement;

    public SignupResponse(String message, String email, String nickname, Boolean privacyAgreement, Boolean publicAgreement) {
        this.message = message;
        this.email = email;
        this.nickname = nickname;
        this.privacyAgreement = privacyAgreement;
        this.publicAgreement = publicAgreement;
    }
}
