package com.ikuzo.tabilog.domain.auth;

import com.ikuzo.tabilog.domain.token.RefreshToken;
import com.ikuzo.tabilog.domain.token.RefreshTokenService;
import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserService;
import com.ikuzo.tabilog.dto.request.FindIdRequest;
import com.ikuzo.tabilog.dto.request.FindPasswordRequest;
import com.ikuzo.tabilog.dto.request.LoginRequest;
import com.ikuzo.tabilog.dto.request.TokenRefreshRequest;
import com.ikuzo.tabilog.dto.request.UserSignupRequest;
import com.ikuzo.tabilog.dto.request.PasswordResetRequest;
import com.ikuzo.tabilog.dto.request.PasswordResetConfirmRequest;
import com.ikuzo.tabilog.dto.response.FindIdResponse;
import com.ikuzo.tabilog.dto.response.FindPasswordResponse;
import com.ikuzo.tabilog.dto.response.JwtResponse;
import com.ikuzo.tabilog.dto.response.SignupResponse;
import com.ikuzo.tabilog.dto.response.TokenRefreshResponse;
import com.ikuzo.tabilog.dto.response.PasswordResetResponse;
import com.ikuzo.tabilog.exception.TokenRefreshException;
import com.ikuzo.tabilog.security.jwt.JwtUtils;
import com.ikuzo.tabilog.security.services.UserDetailsImpl;
import com.ikuzo.tabilog.service.PlanInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final PlanInvitationService planInvitationService;

    // ★ 추가: 액세스 토큰 만료(ms)
    @Value("${tabilog.app.jwtAccessExpirationMs}")
    private long jwtAccessExpirationMs;

    /**
     * 로그인
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 1) 인증 수행
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getId(), loginRequest.getPassword())
        );

        // 2) SecurityContext 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3) Access Token 생성
        String accessToken = jwtUtils.generateJwtToken(authentication);

        // 4) 사용자 정보
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 5) Refresh Token 생성
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        // 6) 만료 시각(epoch millis) 계산
        long expiresAt = System.currentTimeMillis() + jwtAccessExpirationMs;

        // 7) 초대 토큰이 있는 경우 이메일 일치 확인 후 자동 초대 수락 처리
        String redirectUrl = null;
        if (loginRequest.getInvitationToken() != null && !loginRequest.getInvitationToken().trim().isEmpty()) {
            try {
                var invitation = planInvitationService.getInvitationEntityByTokenNoValidate(loginRequest.getInvitationToken());
                System.out.println("초대 정보: token=" + loginRequest.getInvitationToken()
                        + ", inviteeEmail=" + invitation.getInviteeEmail()
                        + ", status=" + invitation.getStatus()
                        + ", planId=" + invitation.getPlan().getId());

                boolean matched = planInvitationService.isInvitationEmailMatched(loginRequest.getInvitationToken(), userDetails.getUsername());
                if (matched && invitation.getStatus().name().equals("PENDING")) {
                    String planRedirectUrl = planInvitationService.acceptInvitation(loginRequest.getInvitationToken(), userDetails.getId(), true);
                    redirectUrl = planRedirectUrl;
                    System.out.println("로그인 시 초대 자동 수락 성공 (PENDING -> ACCEPTED) - 플랜으로 이동: " + redirectUrl);
                } else if (matched) {
                    redirectUrl = "/plans/" + invitation.getPlan().getId();
                    System.out.println("로그인 시 초대 이미 처리됨(status=" + invitation.getStatus() + ") - 플랜으로 이동: " + redirectUrl);
                } else {
                    System.out.println("로그인 시 초대 수락 건너뜀 - 이메일 불일치: 로그인이메일=" + userDetails.getUsername()
                            + ", 초대이메일=" + invitation.getInviteeEmail() + ")");
                }
            } catch (Exception e) {
                System.out.println("로그인 시 초대 자동 수락 실패: " + e.getMessage());
            }
        }

        // 8) 응답: redirectUrl 우선, 아니면 expiresAt 포함 응답
        if (redirectUrl != null) {
            return ResponseEntity.ok(new JwtResponse(
                    accessToken,
                    refreshToken.getToken(),
                    userDetails.getUsername(),
                    userDetails.getUserId(),
                    userDetails.getNickname(),
                    redirectUrl
            ));
        } else {
            return ResponseEntity.ok(new JwtResponse(
                    accessToken,
                    refreshToken.getToken(),
                    userDetails.getUsername(),
                    userDetails.getUserId(),
                    userDetails.getNickname(),
                    expiresAt
            ));
        }
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserSignupRequest signUpRequest) {
        // 초대 토큰 처리 포함 회원가입
        UserService.UserRegistrationResult result = userService.registerWithInvitation(signUpRequest);
        User newUser = result.getUser();
        String redirectUrl = result.getRedirectUrl();

        SignupResponse response;
        if (redirectUrl != null) {
            response = new SignupResponse(
                "회원가입이 성공적으로 완료되었습니다.",
                newUser.getEmail(),
                newUser.getNickname(),
                newUser.getPrivacyAgreement(),
                newUser.getPublicAgreement(),
                redirectUrl
            );
        } else {
            response = new SignupResponse(
                "회원가입이 성공적으로 완료되었습니다.",
                newUser.getEmail(),
                newUser.getNickname(),
                newUser.getPrivacyAgreement(),
                newUser.getPublicAgreement()
            );
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     */
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateJwtTokenFromEmail(user.getEmail());
                    // (선택) 여기서도 expiresAt을 내려주고 싶다면 TokenRefreshResponse에 필드 추가 필요
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    /**
     * 아이디 찾기 (이메일로 user_id 조회)
     */
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@Valid @RequestBody FindIdRequest request) {
        String userId = userService.findIdByEmail(request.getEmail());
        return ResponseEntity.ok(new FindIdResponse(userId));
    }

    /**
     * 비밀번호 찾기 (닉네임과 이메일로 사용자 확인)
     */
    @PostMapping("/find-password")
    public ResponseEntity<?> findPassword(@Valid @RequestBody FindPasswordRequest request) {
        userService.findPasswordByNicknameAndEmail(request.getNickname(), request.getEmail());
        return ResponseEntity.ok(new FindPasswordResponse());
    }

    /**
     * 비밀번호 재설정 요청 (이메일로 인증코드 전송)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        userService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(new PasswordResetResponse());
    }

    /**
     * 비밀번호 재설정 확인 (토큰으로 새 비밀번호 설정)
     */
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<?> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        userService.confirmPasswordReset(request);
        return ResponseEntity.ok(new PasswordResetResponse("비밀번호가 성공적으로 변경되었습니다."));
    }
}
