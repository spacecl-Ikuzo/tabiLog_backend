package com.ikuzo.tabilog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarikanRequest {

    @NotNull(message = "플랜 ID는 필수입니다")
    private Long planId;

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotNull(message = "총 금액은 필수입니다")
    @Positive(message = "총 금액은 양수여야 합니다")
    private Long totalAmount;

    private List<MemberShare> memberShares;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberShare {
        @NotNull(message = "사용자 ID는 필수입니다")
        private Long userId;

        @NotNull(message = "금액은 필수입니다")
        @Positive(message = "금액은 양수여야 합니다")
        private Long amount;
    }
}
