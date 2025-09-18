package com.ikuzo.tabilog.dto.request;

import com.ikuzo.tabilog.domain.plan.PlanMemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlanInvitationRequest {

    @NotBlank(message = "초대할 이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String inviteeEmail;

    @NotNull(message = "역할은 필수입니다.")
    private PlanMemberRole role;
}
