package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.dto.request.PlanRequest;
import com.ikuzo.tabilog.dto.response.PlanResponse;
import com.ikuzo.tabilog.global.ApiResponse;
import com.ikuzo.tabilog.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    public ResponseEntity<ApiResponse<PlanResponse>> createPlan(
            @Valid @RequestBody PlanRequest request,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        PlanResponse response = planService.createPlan(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("여행 계획이 생성되었습니다.", response));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<PlanResponse>> getPlan(
            @PathVariable Long planId,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        PlanResponse response = planService.getPlan(planId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getUserPlans(
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        List<PlanResponse> responses = planService.getUserPlans(userId);
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getActivePlans(
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        List<PlanResponse> responses = planService.getActivePlans(userId);
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getUpcomingPlans(
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        List<PlanResponse> responses = planService.getUpcomingPlans(userId);
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getCompletedPlans(
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        List<PlanResponse> responses = planService.getCompletedPlans(userId);
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{planId}")
    public ResponseEntity<ApiResponse<PlanResponse>> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody PlanRequest request,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        PlanResponse response = planService.updatePlan(planId, request, userId);
        
        return ResponseEntity.ok(ApiResponse.success("여행 계획이 수정되었습니다.", response));
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<ApiResponse<Void>> deletePlan(
            @PathVariable Long planId,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        planService.deletePlan(planId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("여행 계획이 삭제되었습니다.", null));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        // TODO: 실제 인증 정보에서 사용자 ID 추출
        return 1L; // 임시 값
    }
}
