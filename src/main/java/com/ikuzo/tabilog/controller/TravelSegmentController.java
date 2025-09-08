package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.dto.request.TravelSegmentRequest;
import com.ikuzo.tabilog.dto.response.TravelSegmentResponse;
import com.ikuzo.tabilog.global.ApiResponse;
import com.ikuzo.tabilog.service.TravelSegmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travel-segments")
@RequiredArgsConstructor
public class TravelSegmentController {

    private final TravelSegmentService travelSegmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<TravelSegmentResponse>> createTravelSegment(
            @Valid @RequestBody TravelSegmentRequest request) {
        
        TravelSegmentResponse response = travelSegmentService.createTravelSegment(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("이동 구간이 생성되었습니다.", response));
    }

    @GetMapping("/{segmentId}")
    public ResponseEntity<ApiResponse<TravelSegmentResponse>> getTravelSegment(
            @PathVariable Long segmentId) {
        
        TravelSegmentResponse response = travelSegmentService.getTravelSegment(segmentId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/daily-plans/{dailyPlanId}")
    public ResponseEntity<ApiResponse<List<TravelSegmentResponse>>> getTravelSegmentsByDailyPlan(
            @PathVariable Long dailyPlanId) {
        
        List<TravelSegmentResponse> responses = travelSegmentService.getTravelSegmentsByDailyPlan(dailyPlanId);
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{segmentId}")
    public ResponseEntity<ApiResponse<TravelSegmentResponse>> updateTravelSegment(
            @PathVariable Long segmentId,
            @Valid @RequestBody TravelSegmentRequest request) {
        
        TravelSegmentResponse response = travelSegmentService.updateTravelSegment(segmentId, request);
        
        return ResponseEntity.ok(ApiResponse.success("이동 구간이 수정되었습니다.", response));
    }

    @DeleteMapping("/{segmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteTravelSegment(@PathVariable Long segmentId) {
        travelSegmentService.deleteTravelSegment(segmentId);
        
        return ResponseEntity.ok(ApiResponse.success("이동 구간이 삭제되었습니다.", null));
    }

    @PutMapping("/daily-plans/{dailyPlanId}/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderTravelSegments(@PathVariable Long dailyPlanId) {
        travelSegmentService.reorderTravelSegments(dailyPlanId);
        
        return ResponseEntity.ok(ApiResponse.success("이동 구간 순서가 재정렬되었습니다.", null));
    }
}
