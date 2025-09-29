package com.ikuzo.tabilog.domain.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure")
public class TestController {

    @GetMapping("/profile")
    public String getSecureProfile() {
        // 이 API는 JWT 인증을 통과한 사용자만 접근할 수 있습니다.
        return "This is a secure profile page!";
    }
}
