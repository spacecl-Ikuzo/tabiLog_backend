package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.dto.request.PlanInvitationRequest;
import com.ikuzo.tabilog.dto.response.PlanInvitationResponse;
import com.ikuzo.tabilog.dto.response.InvitationCheckResponse;
import com.ikuzo.tabilog.global.ApiResponse;
import com.ikuzo.tabilog.service.PlanInvitationService;
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
public class PlanInvitationController extends BaseController {

    private final PlanInvitationService planInvitationService;

    /**
     * 플랜 멤버 초대
     */
    @PostMapping("/{planId}/invitations")
    public ResponseEntity<ApiResponse<PlanInvitationResponse>> inviteMember(
            @PathVariable Long planId,
            @Valid @RequestBody PlanInvitationRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        PlanInvitationResponse response = planInvitationService.inviteMember(planId, request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("초대 이메일이 전송되었습니다.", response));
    }

    /**
     * 플랜의 초대 목록 조회
     */
    @GetMapping("/{planId}/invitations")
    public ResponseEntity<ApiResponse<List<PlanInvitationResponse>>> getPlanInvitations(
            @PathVariable Long planId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        List<PlanInvitationResponse> responses = planInvitationService.getPlanInvitations(planId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("초대 목록을 조회했습니다.", responses));
    }

    /**
     * 토큰으로 초대 정보 조회
     */
    @GetMapping("/invitations/{token}")
    public ResponseEntity<ApiResponse<PlanInvitationResponse>> getInvitationByToken(
            @PathVariable String token) {
        
        PlanInvitationResponse response = planInvitationService.getInvitationByToken(token);
        
        return ResponseEntity.ok(ApiResponse.success("초대 정보를 조회했습니다.", response));
    }

    /**
     * 토큰으로 초대 정보 확인 (사용자 존재 여부 포함)
     */
    @GetMapping("/invitations/{token}/check")
    public ResponseEntity<ApiResponse<InvitationCheckResponse>> checkInvitation(
            @PathVariable String token,
            Authentication authentication) {
        
        // 기본 응답 (비로그인 또는 이메일 불일치 포함)
        InvitationCheckResponse response = planInvitationService.checkInvitation(token);

        // 로그인 상태이면서 초대 이메일과 로그인 이메일이 일치하면 즉시 수락 처리
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            String loginEmail = getCurrentUserEmail(authentication);
            try {
                boolean matched = planInvitationService.isInvitationEmailMatched(token, loginEmail);
                if (matched) {
                    Long userId = getCurrentUserId(authentication);
                    String redirectUrl = planInvitationService.acceptInvitation(token, userId, true);
                    // 프론트가 동일 응답 스키마를 기대하므로, redirectType은 login으로 유지하고 planId 포함되어 있음
                    // 별도 변경 없이 프론트는 redirectUrl이 오면 그 값 우선 사용
                }
            } catch (Exception ignored) {
                // 수락 실패 시에는 기존 response 그대로 반환
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success("초대 정보를 확인했습니다.", response));
    }
    

    /**
     * 초대 수락
     */
    @PostMapping("/invitations/{token}/accept")
    public ResponseEntity<ApiResponse<String>> acceptInvitation(
            @PathVariable String token,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        String redirectUrl = planInvitationService.acceptInvitation(token, userId, true);
        
        return ResponseEntity.ok(ApiResponse.success("초대를 수락했습니다.", redirectUrl));
    }

    /**
     * 사용자의 대기 중인 초대 목록 조회
     */
    @GetMapping("/invitations")
    public ResponseEntity<ApiResponse<List<PlanInvitationResponse>>> getPendingInvitations(
            Authentication authentication) {
        
        String email = getCurrentUserEmail(authentication);
        List<PlanInvitationResponse> responses = planInvitationService.getPendingInvitations(email);
        
        return ResponseEntity.ok(ApiResponse.success("대기 중인 초대 목록을 조회했습니다.", responses));
    }
}
