package com.ikuzo.tabilog.service;

import com.ikuzo.tabilog.domain.plan.*;
import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserRepository;
import com.ikuzo.tabilog.dto.response.PlanMemberResponse;
import com.ikuzo.tabilog.exception.PlanNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanMemberService {

    private final PlanRepository planRepository;
    private final PlanMemberRepository planMemberRepository;
    private final UserRepository userRepository;

    /**
     * 플랜 멤버 목록 조회
     */
    public List<PlanMemberResponse> getPlanMembers(Long planId, Long userId) {
        // 플랜 조회
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        // 요청자가 플랜 멤버인지 확인
        validatePlanMember(planId, userId);

        // 플랜 멤버 목록 조회
        List<PlanMember> members = planMemberRepository.findByPlanId(planId);

        return members.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 플랜 멤버 역할 변경
     */
    @Transactional
    public PlanMemberResponse updateMemberRole(Long planId, Long memberId, String roleStr, Long userId) {
        PlanMemberRole newRole;
        try {
            newRole = PlanMemberRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("유효하지 않은 역할입니다. OWNER, EDITOR, VIEWER 중 하나여야 합니다.");
        }
        // 플랜 조회
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        // 요청자가 OWNER 또는 EDITOR인지 확인
        validatePermissionForRoleChange(planId, userId);

        // 변경할 멤버 조회 (플랜 ID로도 검증)
        log.info("멤버 조회 시도: planId={}, memberId={}", planId, memberId);
        
        // 먼저 멤버가 존재하는지 확인
        PlanMember memberCheck = planMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("멤버 ID가 존재하지 않습니다: " + memberId));
        log.info("멤버 찾음: planId={}, actualPlanId={}, memberId={}, role={}", 
                planId, memberCheck.getPlan().getId(), memberId, memberCheck.getRole());

        // 이제 플랜 ID로도 검증
        PlanMember member = planMemberRepository.findByIdAndPlanId(memberId, planId)
                .orElseThrow(() -> new RuntimeException("해당 멤버는 이 플랜의 멤버가 아닙니다. (멤버ID: " + memberId + ", 플랜ID: " + planId + ")"));

        // OWNER는 역할 변경 불가
        if (member.getRole() == PlanMemberRole.OWNER) {
            throw new RuntimeException("플랜 소유자의 역할은 변경할 수 없습니다.");
        }

        // OWNER로 변경하려는 경우 거부 (OWNER는 한 명만 가능)
        if (newRole == PlanMemberRole.OWNER) {
            throw new RuntimeException("플랜 소유자 역할로는 변경할 수 없습니다.");
        }

        // 역할 변경
        member.changeRole(newRole);

        log.info("플랜 멤버 역할 변경: planId={}, memberId={}, oldRole={}, newRole={}, changedBy={}", 
                planId, memberId, member.getRole(), newRole, userId);

        return convertToResponse(member);
    }

    /**
     * 플랜 멤버 제거
     */
    @Transactional
    public void removeMember(Long planId, Long memberId, Long userId) {
        // 플랜 조회
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        // 요청자가 OWNER 또는 EDITOR인지 확인
        validatePermissionForMemberRemoval(planId, userId);

        // 제거할 멤버 조회 (플랜 ID로도 검증)
        PlanMember member = planMemberRepository.findByIdAndPlanId(memberId, planId)
                .orElseThrow(() -> new RuntimeException("해당 멤버는 이 플랜의 멤버가 아닙니다."));

        // OWNER는 제거 불가
        if (member.getRole() == PlanMemberRole.OWNER) {
            throw new RuntimeException("플랜 소유자는 제거할 수 없습니다.");
        }

        // 멤버 제거
        planMemberRepository.delete(member);

        log.info("플랜 멤버 제거: planId={}, memberId={}, removedBy={}", 
                planId, memberId, userId);
    }

    private void validatePlanMember(Long planId, Long userId) {
        boolean isMember = planMemberRepository.existsByPlanIdAndUserId(planId, userId);
        if (!isMember) {
            throw new RuntimeException("플랜에 접근할 권한이 없습니다.");
        }
    }

    private void validatePermissionForRoleChange(Long planId, Long userId) {
        PlanMember requester = planMemberRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("플랜에 접근할 권한이 없습니다."));

        if (requester.getRole() != PlanMemberRole.OWNER && requester.getRole() != PlanMemberRole.EDITOR) {
            throw new RuntimeException("멤버 역할 변경 권한이 없습니다. OWNER 또는 EDITOR만 가능합니다.");
        }
    }

    private void validatePermissionForMemberRemoval(Long planId, Long userId) {
        PlanMember requester = planMemberRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("플랜에 접근할 권한이 없습니다."));

        if (requester.getRole() != PlanMemberRole.OWNER && requester.getRole() != PlanMemberRole.EDITOR) {
            throw new RuntimeException("멤버 제거 권한이 없습니다. OWNER 또는 EDITOR만 가능합니다.");
        }
    }

    private PlanMemberResponse convertToResponse(PlanMember member) {
        return PlanMemberResponse.builder()
                .id(member.getId())
                .planId(member.getPlan().getId())
                .userId(member.getUser().getId())
                .userIdString(member.getUser().getUserId()) // 사용자의 userId (String)
                .userEmail(member.getUser().getEmail())
                .userNickname(member.getUser().getNickname())
                .profileImageUrl(member.getUser().getProfileImageUrl()) // user 테이블의 프로필 이미지 URL
                .role(member.getRole())
                .build();
    }
}
