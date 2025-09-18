package com.ikuzo.tabilog.domain.user;

import com.ikuzo.tabilog.dto.request.UserSignupRequest;
import com.ikuzo.tabilog.dto.request.UserProfileUpdateRequest;
import com.ikuzo.tabilog.dto.request.UserPasswordChangeRequest;
import com.ikuzo.tabilog.dto.response.MyPageResponse;
import com.ikuzo.tabilog.exception.DuplicateResourceException;
import com.ikuzo.tabilog.exception.UserNotFoundException;
import com.ikuzo.tabilog.service.PlanInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // 기본적으로 모든 메소드는 읽기 전용 트랜잭션으로 설정합니다.
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlanInvitationService planInvitationService;

    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      @Lazy PlanInvitationService planInvitationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.planInvitationService = planInvitationService;
    }

    /**
     * 회원가입
     */
    @Transactional // 쓰기 작업이므로 readOnly=false로 오버라이드합니다.
    public User register(UserSignupRequest request) {
        // 개인정보 동의 필수 검증
        if (request.getPrivacyAgreement() == null || !request.getPrivacyAgreement()) {
            throw new IllegalArgumentException("개인정보 처리방침에 동의해야 회원가입이 가능합니다.");
        }
        
        // 이메일, 사용자 ID, 닉네임 중복 여부 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateResourceException("이미 사용 중인 사용자 ID입니다: " + request.getUserId());
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다: " + request.getNickname());
        }

        // User 엔티티 생성
        User newUser = User.builder()
                .email(request.getEmail())
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호는 반드시 암호화하여 저장
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .nickname(request.getNickname())
                .privacyAgreement(request.getPrivacyAgreement())
                .publicAgreement(request.getPublicAgreement())
                .build();

        User savedUser = userRepository.save(newUser);

        return savedUser;
    }

    /**
     * 회원가입 with 초대 처리 (초대 토큰 포함)
     */
    @Transactional
    public UserRegistrationResult registerWithInvitation(UserSignupRequest request) {
        User newUser = register(request);
        String redirectUrl = null;

        // 초대 토큰이 있는 경우 이메일 일치 확인 후 자동 초대 수락 처리
        if (request.getInvitationToken() != null && !request.getInvitationToken().trim().isEmpty()) {
            try {
                // 먼저 초대 정보 확인하여 이메일 일치 여부 체크
                if (planInvitationService.isInvitationEmailMatched(request.getInvitationToken(), newUser.getEmail())) {
                    String planRedirectUrl = planInvitationService.acceptInvitation(request.getInvitationToken(), newUser.getId());
                    redirectUrl = planRedirectUrl;
                    System.out.println("회원가입 시 초대 자동 수락 성공 (이메일 일치) - 플랜으로 이동: " + redirectUrl);
                } else {
                    System.out.println("회원가입 시 초대 수락 건너뜀 - 이메일 불일치: 가입이메일=" + newUser.getEmail() + ", 초대토큰=" + request.getInvitationToken());
                }
            } catch (Exception e) {
                // 초대 수락 실패 시 로그만 남기고 회원가입은 계속 진행
                System.out.println("회원가입 시 초대 자동 수락 실패: " + e.getMessage());
            }
        }

        return new UserRegistrationResult(newUser, redirectUrl);
    }

    /**
     * 회원가입 결과를 담는 내부 클래스
     */
    public static class UserRegistrationResult {
        private final User user;
        private final String redirectUrl;

        public UserRegistrationResult(User user, String redirectUrl) {
            this.user = user;
            this.redirectUrl = redirectUrl;
        }

        public User getUser() {
            return user;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }
    }

    /**
     * 사용자 정보 조회 (ID 기반)
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    /**
     * 사용자 정보 조회 (이메일 기반)
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. 이메일: " + email));
    }

    /**
     * 프로필 정보 업데이트
     */
    @Transactional
    public User updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = getUserById(userId);

        // 닉네임 변경 시 중복 검사 (자신의 닉네임은 제외)
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname()) &&
                userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다.");
        }

        // Entity의 update 메소드를 호출하여 변경 (Dirty Checking 활용)
        user.updateProfile(request.getNickname(), request.getPhoneNumber());

        return user;
        // @Transactional 어노테이션 덕분에 user 객체가 변경되면,
        // 메소드 종료 시 자동으로 DB에 업데이트 쿼리가 실행됩니다. (userRepository.save() 호출 필요 없음)
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, UserPasswordChangeRequest request) {
        User user = getUserById(userId);

        // 현재 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            // 실제 서비스에서는 더 일반적인 메시지를 사용하는 것이 보안상 좋습니다.
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 새 비밀번호와 확인용 비밀번호가 일치하는지 확인
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호로 업데이트
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    /**
     * 회원 탈퇴 (계정 삭제)
     */
    @Transactional
    public void deleteAccount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    /**
     * 아이디 찾기 (이메일로 닉네임 조회)
     */
    public String findIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자를 찾을 수 없습니다."));
        
        return user.getNickname();
    }

    /**
     * 비밀번호 찾기 (닉네임과 이메일로 사용자 확인)
     */
    public void findPasswordByNicknameAndEmail(String nickname, String email) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserNotFoundException("해당 닉네임으로 등록된 사용자를 찾을 수 없습니다."));
        
        if (!user.getEmail().equals(email)) {
            throw new UserNotFoundException("닉네임과 이메일이 일치하지 않습니다.");
        }
        
        // TODO: 비밀번호 재설정 이메일 발송 로직 구현
        // 현재는 사용자 확인만 수행
    }

    /**
     * 마이페이지 정보 조회
     */
    public MyPageResponse getMyPageInfo(Long userId) {
        User user = getUserById(userId);
        
        return new MyPageResponse(
            user.getId(),
            user.getEmail(),
            user.getUserId(),
            user.getFirstName(),
            user.getLastName(),
            user.getGender(),
            user.getPhoneNumber(),
            user.getNickname(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getParticipatingPlans().size(),
            user.getOwnedPlans().size()
        );
    }
}