package com.ikuzo.tabilog.dto.response;

import com.ikuzo.tabilog.domain.plan.PlanMemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvitationCheckResponse {
    private String inviteeEmail;
    private String planTitle;
    private String inviterName;
    private PlanMemberRole role;
    private boolean userExists; // 사용자가 이미 존재하는지 여부
    private String redirectType; // "login" 또는 "signup"
}
