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
import com.ikuzo.tabilog.dto.response.FindIdResponse;
import com.ikuzo.tabilog.dto.response.FindPasswordResponse;
import com.ikuzo.tabilog.dto.response.JwtResponse;
import com.ikuzo.tabilog.dto.response.SignupResponse;
import com.ikuzo.tabilog.dto.response.TokenRefreshResponse;
import com.ikuzo.tabilog.exception.TokenRefreshException;
import com.ikuzo.tabilog.security.jwt.JwtUtils;
import com.ikuzo.tabilog.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 사용자 인증 수행
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getId(), loginRequest.getPassword()));

        // SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // JWT Access Token 생성
        String accessToken = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Refresh Token 생성
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken.getToken(), userDetails.getUsername(), userDetails.getUserId()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserSignupRequest signUpRequest) {
        User newUser = userService.register(signUpRequest);
        
        SignupResponse response = new SignupResponse(
            "회원가입이 성공적으로 완료되었습니다.",
            newUser.getEmail(),
            newUser.getNickname(),
            newUser.getPrivacyAgreement(),
            newUser.getPublicAgreement()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateJwtTokenFromEmail(user.getEmail());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    /**
     * 아이디 찾기 (이메일로 닉네임 조회)
     */
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@Valid @RequestBody FindIdRequest request) {
        String nickname = userService.findIdByEmail(request.getEmail());
        return ResponseEntity.ok(new FindIdResponse(nickname));
    }

    /**
     * 비밀번호 찾기 (닉네임과 이메일로 사용자 확인)
     */
    @PostMapping("/find-password")
    public ResponseEntity<?> findPassword(@Valid @RequestBody FindPasswordRequest request) {
        userService.findPasswordByNicknameAndEmail(request.getNickname(), request.getEmail());
        return ResponseEntity.ok(new FindPasswordResponse());
    }
}

