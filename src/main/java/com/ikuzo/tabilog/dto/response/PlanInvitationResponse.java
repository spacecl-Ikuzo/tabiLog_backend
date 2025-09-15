package com.ikuzo.tabilog.dto.response;

import com.ikuzo.tabilog.domain.invitation.InvitationStatus;
import com.ikuzo.tabilog.domain.plan.PlanMemberRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PlanInvitationResponse {
    private Long id;
    private Long planId;
    private String planTitle;
    private String inviteeEmail;
    private String inviterName;
    private PlanMemberRole role;
    private InvitationStatus status;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
