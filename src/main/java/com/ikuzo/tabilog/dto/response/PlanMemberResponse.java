package com.ikuzo.tabilog.dto.response;

import com.ikuzo.tabilog.domain.plan.PlanMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanMemberResponse {

    private Long id;
    private Long userId;
    private String userIdString; // 사용자의 userId (String)
    private String userNickname;
    private String userEmail;
    private PlanMemberRole role;
}
