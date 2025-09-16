package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.domain.user.UserService;
import com.ikuzo.tabilog.dto.response.MyPageResponse;
import com.ikuzo.tabilog.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
}
