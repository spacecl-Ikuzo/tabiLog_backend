package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.dto.request.WarikanRequest;
import com.ikuzo.tabilog.dto.response.WarikanResponse;
import com.ikuzo.tabilog.global.ApiResponse;
import com.ikuzo.tabilog.service.WarikanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/warikan")
@RequiredArgsConstructor
public class WarikanController extends BaseController {

    private final WarikanService warikanService;

    /**
     * 플랜 멤버들에게 와리깡 정보 이메일 전송
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<WarikanResponse>> sendWarikan(
            @Valid @RequestBody WarikanRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        log.info("와리깡 이메일 전송 요청: planId={}, senderId={}, title={}", 
                request.getPlanId(), userId, request.getTitle());
        
        WarikanResponse response = warikanService.sendWarikanToMembers(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("와리깡 정보가 멤버들에게 전송되었습니다.", response));
    }
}
