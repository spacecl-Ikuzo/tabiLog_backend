package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.domain.user.UserService;
import com.ikuzo.tabilog.dto.response.MyPageResponse;
import com.ikuzo.tabilog.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPage(Authentication authentication) {
        Long userId = AuthUtils.getUserIdFromAuthentication(authentication);
        MyPageResponse response = userService.getMyPageInfo(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원탈퇴
     */
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(Authentication authentication) {
        Long userId = AuthUtils.getUserIdFromAuthentication(authentication);
        userService.deleteAccount(userId);
        return ResponseEntity.ok("회원탈퇴가 완료되었습니다.");
    }
}
