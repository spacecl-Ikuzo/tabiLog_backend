package com.ikuzo.tabilog.service;

import com.ikuzo.tabilog.domain.plan.DailyPlan;
import com.ikuzo.tabilog.domain.plan.DailyPlanRepository;
import com.ikuzo.tabilog.domain.plan.Plan;
import com.ikuzo.tabilog.domain.plan.PlanRepository;
import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserRepository;
import com.ikuzo.tabilog.dto.request.DailyPlanRequest;
import com.ikuzo.tabilog.dto.request.PlanRequest;
import com.ikuzo.tabilog.dto.response.DailyPlanResponse;
import com.ikuzo.tabilog.dto.response.PlanResponse;
import com.ikuzo.tabilog.exception.PlanNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanService {

    private final PlanRepository planRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final UserRepository userRepository;

    @Transactional
    public PlanResponse createPlan(PlanRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        Plan plan = Plan.builder()
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalBudget(request.getTotalBudget())
                .user(user)
                .build();

        Plan savedPlan = planRepository.save(plan);

        // 일별 계획 생성
        if (request.getDailyPlans() != null) {
            for (DailyPlanRequest dailyPlanRequest : request.getDailyPlans()) {
                DailyPlan dailyPlan = DailyPlan.builder()
                        .plan(savedPlan)
                        .visitDate(dailyPlanRequest.getVisitDate())
                        .departureTime(dailyPlanRequest.getDepartureTime())
                        .build();
                savedPlan.addDailyPlan(dailyPlan);
            }
        }

        return convertToResponse(savedPlan);
    }

    public PlanResponse getPlan(Long planId, Long userId) {
        Plan plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new PlanNotFoundException(planId));
        return convertToResponse(plan);
    }

    public List<PlanResponse> getUserPlans(Long userId) {
        List<Plan> plans = planRepository.findAllByUserIdOrderByStartDateDesc(userId);
        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PlanResponse> getActivePlans(Long userId) {
        List<Plan> plans = planRepository.findActivePlansByUserId(userId);
        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PlanResponse> getUpcomingPlans(Long userId) {
        List<Plan> plans = planRepository.findUpcomingPlansByUserId(userId);
        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PlanResponse> getCompletedPlans(Long userId) {
        List<Plan> plans = planRepository.findCompletedPlansByUserId(userId);
        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PlanResponse updatePlan(Long planId, PlanRequest request, Long userId) {
        Plan plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        plan.updatePlan(request.getTitle(), request.getStartDate(), 
                       request.getEndDate(), request.getTotalBudget());

        return convertToResponse(plan);
    }

    @Transactional
    public void deletePlan(Long planId, Long userId) {
        Plan plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new PlanNotFoundException(planId));
        planRepository.delete(plan);
    }

    private PlanResponse convertToResponse(Plan plan) {
        List<DailyPlanResponse> dailyPlanResponses = plan.getDailyPlans().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PlanResponse.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .totalBudget(plan.getTotalBudget())
                .userId(plan.getUser().getId())
                .dailyPlans(dailyPlanResponses)
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private DailyPlanResponse convertToResponse(DailyPlan dailyPlan) {
        return DailyPlanResponse.builder()
                .id(dailyPlan.getId())
                .visitDate(dailyPlan.getVisitDate())
                .departureTime(dailyPlan.getDepartureTime())
                .spots(null) // SpotService에서 처리
                .travelSegments(null) // TravelSegmentService에서 처리
                .createdAt(dailyPlan.getCreatedAt())
                .updatedAt(dailyPlan.getUpdatedAt())
                .build();
    }
}
