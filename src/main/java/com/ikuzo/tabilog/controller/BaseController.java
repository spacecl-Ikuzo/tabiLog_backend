package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.security.AuthUtils;
import org.springframework.security.core.Authentication;

/**
 * 모든 컨트롤러의 베이스 클래스
 * 공통적으로 사용되는 메소드들을 제공합니다.
 */
public abstract class BaseController {

    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     * @param authentication Spring Security Authentication 객체
     * @return 사용자 ID (Long)
     */
    protected Long getCurrentUserId(Authentication authentication) {
        return AuthUtils.getUserIdFromAuthentication(authentication);
    }

    /**
     * 현재 인증된 사용자의 사용자 ID (user_id 필드)를 반환합니다.
     * @param authentication Spring Security Authentication 객체
     * @return 사용자 ID (String)
     */
    protected String getCurrentUserIdString(Authentication authentication) {
        return AuthUtils.getUserIdStringFromAuthentication(authentication);
    }

    /**
     * 현재 인증된 사용자의 이메일을 반환합니다.
     * @param authentication Spring Security Authentication 객체
     * @return 사용자 이메일 (String)
     */
    protected String getCurrentUserEmail(Authentication authentication) {
        return AuthUtils.getEmailFromAuthentication(authentication);
    }
}
