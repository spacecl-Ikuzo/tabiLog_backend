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
    private String region;
    private String prefecture;
    private String prefectureImageUrl;
    private Long participant_count;
    private Long totalBudget;
    private String status;
    private Long userId;
    private List<DailyPlanResponse> dailyPlans;
    private List<PlanMemberResponse> members;
    private List<ExpenseResponse> expenses;
    private Long totalExpenseAmount; // 총 지출 금액
    private boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
