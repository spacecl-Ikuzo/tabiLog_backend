package com.ikuzo.tabilog.security;

import com.ikuzo.tabilog.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     * @param authentication Spring Security Authentication 객체
     * @return 사용자 ID (Long)
     * @throws RuntimeException 인증되지 않았거나 올바르지 않은 인증 정보인 경우
     */
    public static Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("인증이 필요합니다. 로그인 후 다시 시도해주세요.");
        }
        
        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getId();
        }
        
        throw new RuntimeException("올바르지 않은 인증 정보입니다.");
    }

    /**
     * SecurityContextHolder에서 현재 인증된 사용자의 ID를 반환합니다.
     * @return 사용자 ID (Long)
     * @throws RuntimeException 인증되지 않았거나 올바르지 않은 인증 정보인 경우
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getUserIdFromAuthentication(authentication);
    }

    /**
     * 현재 인증된 사용자의 사용자 ID (user_id 필드)를 반환합니다.
     * @param authentication Spring Security Authentication 객체
     * @return 사용자 ID (String)
     * @throws RuntimeException 인증되지 않았거나 올바르지 않은 인증 정보인 경우
     */
    public static String getUserIdStringFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("인증이 필요합니다. 로그인 후 다시 시도해주세요.");
        }
        
        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getUserId();
        }
        
        throw new RuntimeException("올바르지 않은 인증 정보입니다.");
    }

    /**
     * 현재 인증된 사용자의 이메일을 반환합니다.
     * @param authentication Spring Security Authentication 객체
     * @return 사용자 이메일 (String)
     * @throws RuntimeException 인증되지 않았거나 올바르지 않은 인증 정보인 경우
     */
    public static String getEmailFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("인증이 필요합니다. 로그인 후 다시 시도해주세요.");
        }
        
        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getEmail();
        }
        
        throw new RuntimeException("올바르지 않은 인증 정보입니다.");
    }

    /**
     * 현재 인증된 사용자의 UserDetailsImpl 객체를 반환합니다.
     * @param authentication Spring Security Authentication 객체
     * @return UserDetailsImpl 객체
     * @throws RuntimeException 인증되지 않았거나 올바르지 않은 인증 정보인 경우
     */
    public static UserDetailsImpl getUserDetailsFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("인증이 필요합니다. 로그인 후 다시 시도해주세요.");
        }
        
        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        
        throw new RuntimeException("올바르지 않은 인증 정보입니다.");
    }
}
