package com.ikuzo.tabilog.service;

import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserRepository;
import com.ikuzo.tabilog.dto.request.ProfileImageUpdateRequest;
import com.ikuzo.tabilog.dto.request.ProfileUpdateRequest;
import com.ikuzo.tabilog.dto.response.ProfileResponse;
import com.ikuzo.tabilog.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 프로필 조회
     */
    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        return convertToResponse(user);
    }

    /**
     * 사용자 프로필 업데이트
     */
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("現在のパスワードが正しくありません");
        }

        // 이메일 중복 확인 (본인 제외)
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
                throw new IllegalArgumentException("このメールアドレスは既に使用されています");
            }
        }

        // 닉네임 중복 확인 (본인 제외)
        if (!user.getNickname().equals(request.getNickname())) {
            if (userRepository.existsByNicknameAndIdNot(request.getNickname(), userId)) {
                throw new IllegalArgumentException("このニックネームは既に使用されています");
            }
        }

        // 프로필 정보 업데이트
        user.updateProfile(
                request.getFirstName(),
                request.getLastName(),
                request.getNickname(),
                request.getPhoneNumber()
        );

        // 이메일 업데이트
        user.updateEmail(request.getEmail());

        // 새 비밀번호가 제공된 경우 업데이트
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new IllegalArgumentException("新しいパスワードと確認パスワードが一致しません");
            }
            user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        }

        User savedUser = userRepository.save(user);
        log.info("사용자 프로필 업데이트 완료: userId={}", userId);
        
        return convertToResponse(savedUser);
    }

    /**
     * 프로필 이미지 업데이트
     */
    @Transactional
    public ProfileResponse updateProfileImage(Long userId, ProfileImageUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.updateProfileImage(request.getProfileImageUrl());
        
        User savedUser = userRepository.save(user);
        log.info("사용자 프로필 이미지 업데이트 완료: userId={}", userId);
        
        return convertToResponse(savedUser);
    }

    /**
     * User 엔티티를 ProfileResponse로 변환
     */
    private ProfileResponse convertToResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}



