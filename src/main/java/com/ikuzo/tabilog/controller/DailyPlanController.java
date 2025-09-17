package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.domain.plan.DailyPlan;
import com.ikuzo.tabilog.domain.plan.DailyPlanRepository;
import com.ikuzo.tabilog.domain.plan.Plan;
import com.ikuzo.tabilog.domain.plan.PlanRepository;
import com.ikuzo.tabilog.dto.request.DailyPlanRequest;
import com.ikuzo.tabilog.dto.response.DailyPlanResponse;
import com.ikuzo.tabilog.dto.response.SpotResponse;
import com.ikuzo.tabilog.dto.response.TravelSegmentResponse;
import com.ikuzo.tabilog.global.ApiResponse;
import com.ikuzo.tabilog.service.SpotService;
import com.ikuzo.tabilog.service.TravelSegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/daily-plans")
@RequiredArgsConstructor
public class DailyPlanController extends BaseController {

    private final DailyPlanRepository dailyPlanRepository;
    private final PlanRepository planRepository;
    private final SpotService spotService;
    private final TravelSegmentService travelSegmentService;

    @PostMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<DailyPlanResponse>> createDailyPlan(
            @PathVariable Long planId,
            @Valid @RequestBody DailyPlanRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("계획을 찾을 수 없습니다: " + planId));
        
        // Plan의 소유자 확인
        if (!plan.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 계획에 접근할 권한이 없습니다.");
        }

        // 같은 날짜의 DailyPlan이 이미 존재하는지 확인
        List<DailyPlan> existingDailyPlans = dailyPlanRepository.findByPlanAndVisitDate(plan, request.getVisitDate());
        if (!existingDailyPlans.isEmpty()) {
            // 이미 존재하는 경우 기존 DailyPlan 반환
            DailyPlan existingDailyPlan = existingDailyPlans.get(0);
            DailyPlanResponse response = DailyPlanResponse.builder()
                    .id(existingDailyPlan.getId())
                    .visitDate(existingDailyPlan.getVisitDate())
                    .departureTime(existingDailyPlan.getDepartureTime())
                    .spots(List.of()) // 빈 리스트
                    .travelSegments(List.of()) // 빈 리스트
                    .createdAt(existingDailyPlan.getCreatedAt())
                    .updatedAt(existingDailyPlan.getUpdatedAt())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("일별 계획이 이미 존재합니다.", response));
        }

        DailyPlan dailyPlan = DailyPlan.builder()
                .plan(plan)
                .visitDate(request.getVisitDate())
                .departureTime(request.getDepartureTime())
                .build();

        DailyPlan savedDailyPlan = dailyPlanRepository.save(dailyPlan);

        DailyPlanResponse response = DailyPlanResponse.builder()
                .id(savedDailyPlan.getId())
                .visitDate(savedDailyPlan.getVisitDate())
                .departureTime(savedDailyPlan.getDepartureTime())
                .spots(List.of()) // 빈 리스트
                .travelSegments(List.of()) // 빈 리스트
                .createdAt(savedDailyPlan.getCreatedAt())
                .updatedAt(savedDailyPlan.getUpdatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success("일별 계획이 생성되었습니다.", response));
    }

    @GetMapping("/{dailyPlanId}")
    public ResponseEntity<ApiResponse<DailyPlanResponse>> getDailyPlan(
            @PathVariable Long dailyPlanId,
            Authentication authentication) {
        
        Long userId = authentication != null ? getCurrentUserId(authentication) : null;
        DailyPlan dailyPlan = dailyPlanRepository.findById(dailyPlanId)
                .orElseThrow(() -> new RuntimeException("일별 계획을 찾을 수 없습니다: " + dailyPlanId));

        List<SpotResponse> spots = spotService.getSpotsByDailyPlan(dailyPlanId, userId);
        List<TravelSegmentResponse> travelSegments = travelSegmentService.getTravelSegmentsByDailyPlan(dailyPlanId);

        DailyPlanResponse response = DailyPlanResponse.builder()
                .id(dailyPlan.getId())
                .visitDate(dailyPlan.getVisitDate())
                .departureTime(dailyPlan.getDepartureTime())
                .spots(spots)
                .travelSegments(travelSegments)
                .createdAt(dailyPlan.getCreatedAt())
                .updatedAt(dailyPlan.getUpdatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{dailyPlanId}/departure-time")
    public ResponseEntity<ApiResponse<DailyPlanResponse>> updateDepartureTime(
            @PathVariable Long dailyPlanId,
            @RequestParam String departureTime,
            Authentication authentication) {
        
        Long userId = authentication != null ? getCurrentUserId(authentication) : null;
        DailyPlan dailyPlan = dailyPlanRepository.findById(dailyPlanId)
                .orElseThrow(() -> new RuntimeException("일별 계획을 찾을 수 없습니다: " + dailyPlanId));

        LocalTime time = LocalTime.parse(departureTime);
        dailyPlan.updateDepartureTime(time);
        dailyPlanRepository.save(dailyPlan);

        List<SpotResponse> spots = spotService.getSpotsByDailyPlan(dailyPlanId, userId);
        List<TravelSegmentResponse> travelSegments = travelSegmentService.getTravelSegmentsByDailyPlan(dailyPlanId);

        DailyPlanResponse response = DailyPlanResponse.builder()
                .id(dailyPlan.getId())
                .visitDate(dailyPlan.getVisitDate())
                .departureTime(dailyPlan.getDepartureTime())
                .spots(spots)
                .travelSegments(travelSegments)
                .createdAt(dailyPlan.getCreatedAt())
                .updatedAt(dailyPlan.getUpdatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success("출발시간이 수정되었습니다.", response));
    }
}
