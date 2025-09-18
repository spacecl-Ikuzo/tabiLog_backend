package com.ikuzo.tabilog.dto.request;

import com.ikuzo.tabilog.domain.plan.PlanMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlanMemberRoleUpdateRequest {

    @NotNull(message = "역할은 필수입니다.")
    private String role;
}
