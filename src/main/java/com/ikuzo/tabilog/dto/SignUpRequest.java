package com.ikuzo.tabilog.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignUpRequest {

    @NotBlank
    @JsonAlias({"username", "accountId", "loginId"})
    private String id;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @JsonAlias({"handleName", "displayName"})
    private String nickname;

    private Boolean privacyAgreement = Boolean.TRUE;
    private Boolean publicAgreement = Boolean.FALSE;
}
