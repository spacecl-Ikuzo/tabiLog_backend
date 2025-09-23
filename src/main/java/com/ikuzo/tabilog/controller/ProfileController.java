package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.dto.request.ProfileImageUpdateRequest;
import com.ikuzo.tabilog.dto.request.ProfileUpdateRequest;
import com.ikuzo.tabilog.global.ApiResponse;
import com.ikuzo.tabilog.dto.response.ProfileResponse;
import com.ikuzo.tabilog.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController extends BaseController {

    private final ProfileService profileService;

    /**
     * 사용자 프로필 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            ProfileResponse profile = profileService.getProfile(userId);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "프로필 조회가 성공적으로 완료되었습니다.",
                    profile
            ));
        } catch (Exception e) {
            log.error("프로필 조회 실패", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "프로필 조회에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 사용자 프로필 업데이트
     */
    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            ProfileResponse updatedProfile = profileService.updateProfile(userId, request);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "프로필이 성공적으로 업데이트되었습니다.",
                    updatedProfile
            ));
        } catch (IllegalArgumentException e) {
            log.warn("프로필 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("프로필 업데이트 실패", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "프로필 업데이트에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 프로필 이미지 업데이트
     */
    @PutMapping("/image")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfileImage(
            @Valid @RequestBody ProfileImageUpdateRequest request,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            ProfileResponse updatedProfile = profileService.updateProfileImage(userId, request);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "프로필 이미지가 성공적으로 업데이트되었습니다.",
                    updatedProfile
            ));
        } catch (Exception e) {
            log.error("프로필 이미지 업데이트 실패", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "프로필 이미지 업데이트에 실패했습니다: " + e.getMessage()
            ));
        }
    }
}
