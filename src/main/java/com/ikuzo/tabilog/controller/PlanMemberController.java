package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.dto.request.PlanMemberRoleUpdateRequest;
import com.ikuzo.tabilog.dto.response.PlanMemberResponse;
import com.ikuzo.tabilog.global.ApiResponse;
import com.ikuzo.tabilog.service.PlanMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanMemberController extends BaseController {

    private final PlanMemberService planMemberService;

    /**
     * 플랜 멤버 목록 조회
     */
    @GetMapping("/{planId}/members")
    public ResponseEntity<ApiResponse<List<PlanMemberResponse>>> getPlanMembers(
            @PathVariable Long planId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        List<PlanMemberResponse> members = planMemberService.getPlanMembers(planId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("플랜 멤버 목록을 조회했습니다.", members));
    }

    /**
     * 플랜 멤버 역할 변경
     */
    @PutMapping("/{planId}/members/{memberId}/role")
    public ResponseEntity<ApiResponse<PlanMemberResponse>> updateMemberRole(
            @PathVariable Long planId,
            @PathVariable Long memberId,
            @Valid @RequestBody PlanMemberRoleUpdateRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        PlanMemberResponse response = planMemberService.updateMemberRole(planId, memberId, request.getRole(), userId);
        
        return ResponseEntity.ok(ApiResponse.success("멤버 역할이 변경되었습니다.", response));
    }

    /**
     * 플랜 멤버 제거
     */
    @DeleteMapping("/{planId}/members/{memberId}")
    public ResponseEntity<ApiResponse<String>> removeMember(
            @PathVariable Long planId,
            @PathVariable Long memberId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        planMemberService.removeMember(planId, memberId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("멤버가 제거되었습니다.", "플랜에서 멤버가 성공적으로 제거되었습니다."));
    }
}
