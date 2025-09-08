package com.ikuzo.tabilog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponse {

    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalBudget;
    private Long userId;
    private List<DailyPlanResponse> dailyPlans;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
