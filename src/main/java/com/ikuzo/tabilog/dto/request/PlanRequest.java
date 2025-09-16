package com.ikuzo.tabilog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequest {

    @NotBlank(message = "여행 제목은 필수입니다")
    private String title;

    @NotNull(message = "시작 날짜는 필수입니다")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수입니다")
    private LocalDate endDate;

    @NotNull(message = "총 예산은 필수입니다")
    @PositiveOrZero(message = "총 예산은 0 이상이어야 합니다")
    private Long totalBudget;

    @NotBlank(message = "지역은 필수입니다")
    private String region;

    @NotBlank(message = "현은 필수입니다")
    private String prefecture;

    private String prefectureImageUrl;

    private List<DailyPlanRequest> dailyPlans;
}
