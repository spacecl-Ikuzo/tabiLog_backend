package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.domain.plan.DailyPlan;
import com.ikuzo.tabilog.domain.plan.DailyPlanRepository;
import com.ikuzo.tabilog.dto.response.DailyPlanResponse;
import com.ikuzo.tabilog.dto.response.SpotResponse;
import com.ikuzo.tabilog.dto.response.TravelSegmentResponse;
import com.ikuzo.tabilog.global.ApiResponse;
import com.ikuzo.tabilog.service.SpotService;
import com.ikuzo.tabilog.service.TravelSegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/daily-plans")
@RequiredArgsConstructor
public class DailyPlanController {

    private final DailyPlanRepository dailyPlanRepository;
    private final SpotService spotService;
    private final TravelSegmentService travelSegmentService;

    @GetMapping("/{dailyPlanId}")
    public ResponseEntity<ApiResponse<DailyPlanResponse>> getDailyPlan(@PathVariable Long dailyPlanId) {
        DailyPlan dailyPlan = dailyPlanRepository.findById(dailyPlanId)
                .orElseThrow(() -> new RuntimeException("일별 계획을 찾을 수 없습니다: " + dailyPlanId));

        List<SpotResponse> spots = spotService.getSpotsByDailyPlan(dailyPlanId);
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
            @RequestParam String departureTime) {
        
        DailyPlan dailyPlan = dailyPlanRepository.findById(dailyPlanId)
                .orElseThrow(() -> new RuntimeException("일별 계획을 찾을 수 없습니다: " + dailyPlanId));

        LocalTime time = LocalTime.parse(departureTime);
        dailyPlan.updateDepartureTime(time);
        dailyPlanRepository.save(dailyPlan);

        List<SpotResponse> spots = spotService.getSpotsByDailyPlan(dailyPlanId);
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
