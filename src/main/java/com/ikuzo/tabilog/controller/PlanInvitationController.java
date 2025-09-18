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
            @PathVariable String token) {
        
        InvitationCheckResponse response = planInvitationService.checkInvitation(token);
        
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
        String redirectUrl = planInvitationService.acceptInvitation(token, userId);
        
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
