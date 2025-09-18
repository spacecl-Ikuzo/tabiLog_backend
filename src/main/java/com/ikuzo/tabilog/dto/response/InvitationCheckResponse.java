package com.ikuzo.tabilog.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvitationCheckResponse {
    private Long planId;
    private String planTitle;
    private String role;
    private String inviteeEmail;
    private boolean userExists; // 사용자가 이미 존재하는지 여부
    private String redirectType; // "login" 또는 "register"
}
