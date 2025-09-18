package com.ikuzo.tabilog.service;

import com.ikuzo.tabilog.domain.invitation.InvitationStatus;
import com.ikuzo.tabilog.domain.invitation.PlanInvitation;
import com.ikuzo.tabilog.domain.invitation.PlanInvitationRepository;
import com.ikuzo.tabilog.domain.plan.Plan;
import com.ikuzo.tabilog.domain.plan.PlanMember;
import com.ikuzo.tabilog.domain.plan.PlanMemberRepository;
import com.ikuzo.tabilog.domain.plan.PlanMemberRole;
import com.ikuzo.tabilog.domain.plan.PlanRepository;
import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserRepository;
import com.ikuzo.tabilog.dto.request.PlanInvitationRequest;
import com.ikuzo.tabilog.dto.response.PlanInvitationResponse;
import com.ikuzo.tabilog.dto.response.InvitationCheckResponse;
import com.ikuzo.tabilog.exception.PlanNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanInvitationService {

    private final PlanInvitationRepository planInvitationRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final PlanMemberRepository planMemberRepository;
    private final EmailService emailService;

    /**
     * 플랜 멤버 초대
     */
    @Transactional
    public PlanInvitationResponse inviteMember(Long planId, PlanInvitationRequest request, Long inviterId) {
        // 플랜 조회
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        // 초대자 권한 확인 (OWNER 또는 EDITOR만 초대 가능)
        validateInviterPermission(plan, inviterId);

        // 이미 멤버인지 확인
        validateNotAlreadyMember(planId, request.getInviteeEmail());

        // 기존 대기 중인 초대가 있는지 확인
        validateNoPendingInvitation(planId, request.getInviteeEmail());

        // 초대자 정보 조회
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new RuntimeException("초대자를 찾을 수 없습니다."));

        // 초대 토큰 생성
        String token = generateInvitationToken();

        // 초대 엔티티 생성
        PlanInvitation invitation = PlanInvitation.builder()
                .plan(plan)
                .inviteeEmail(request.getInviteeEmail())
                .token(token)
                .role(request.getRole())
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7일 후 만료
                .build();

        PlanInvitation savedInvitation = planInvitationRepository.save(invitation);

        // 이메일 전송
        try {
            emailService.sendPlanInvitationEmail(
                    request.getInviteeEmail(),
                    inviter.getNickname(),
                    plan.getTitle(),
                    token
            );
        } catch (Exception e) {
            log.error("초대 이메일 전송 실패: {}", e.getMessage());
            // 이메일 전송 실패 시에도 초대는 생성되도록 함
        }

        return convertToResponse(savedInvitation, inviter.getNickname());
    }

    /**
     * 초대 수락 처리
     *
     * @param token 초대 토큰
     * @param userId 수락하는 사용자 ID
     * @param deleteAfterAccept 수락 후 초대 레코드를 삭제할지 여부
     */
    @Transactional
    public String acceptInvitation(String token, Long userId, boolean deleteAfterAccept) {
        // 토큰으로 초대 조회
        PlanInvitation invitation = planInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 초대 링크입니다."));

        // 초대 유효성 검증
        validateInvitation(invitation);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이메일 일치 확인
        if (!user.getEmail().equals(invitation.getInviteeEmail())) {
            throw new RuntimeException("초대된 이메일과 로그인한 계정의 이메일이 일치하지 않습니다.");
        }

        // 이미 멤버인지 재확인
        validateNotAlreadyMember(invitation.getPlan().getId(), user.getEmail());

        // 플랜 멤버로 추가
        PlanMember planMember = PlanMember.builder()
                .plan(invitation.getPlan())
                .user(user)
                .role(invitation.getRole())
                .build();

        planMemberRepository.save(planMember);

        // 초대 상태를 수락으로 변경
        invitation.accept();

        // 필요 시 초대 데이터 삭제 (로그인 처리 등)
        if (deleteAfterAccept) {
            planInvitationRepository.delete(invitation);
            log.info("플랜 초대 수락 완료 및 초대 데이터 삭제: planId={}, userId={}, email={}",
                    invitation.getPlan().getId(), userId, user.getEmail());
        } else {
            log.info("플랜 초대 수락 완료 (레코드 유지): planId={}, userId={}, email={}",
                    invitation.getPlan().getId(), userId, user.getEmail());
        }

        return "/plans/" + invitation.getPlan().getId(); // 리다이렉트할 플랜 페이지 URL
    }

    /**
     * 초대 토큰의 이메일과 사용자 이메일이 일치하는지 확인
     */
    public boolean isInvitationEmailMatched(String token, String userEmail) {
        try {
            PlanInvitation invitation = planInvitationRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("유효하지 않은 초대 링크입니다."));
            boolean matched = invitation.getInviteeEmail() != null && userEmail != null
                    ? invitation.getInviteeEmail().trim().equalsIgnoreCase(userEmail.trim())
                    : false;
            log.info("이메일 매칭 검사: token={}, inviteeEmail={}, loginEmail={}, matched={}",
                    token, invitation.getInviteeEmail(), userEmail, matched);
            return matched;
        } catch (Exception e) {
            log.error("초대 이메일 일치 확인 실패: token={}, userEmail={}, error={}", token, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * 토큰으로 초대 엔티티 조회 (검증 없이)
     */
    public PlanInvitation getInvitationEntityByTokenNoValidate(String token) {
        return planInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 초대 링크입니다."));
    }

    /**
     * 토큰으로 초대 정보 조회 (이메일 확인용)
     */
    public PlanInvitationResponse getInvitationByToken(String token) {
        PlanInvitation invitation = planInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 초대 링크입니다."));

        validateInvitation(invitation);

        return convertToResponse(invitation, null);
    }

    /**
     * 초대 토큰으로 초대 정보 확인 (사용자 존재 여부 포함)
     */
    public InvitationCheckResponse checkInvitation(String token) {
        PlanInvitation invitation = planInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 초대 링크입니다."));

        validateInvitation(invitation);

        // 사용자 존재 여부 확인
        Optional<User> userOpt = userRepository.findByEmail(invitation.getInviteeEmail());
        boolean userExists = userOpt.isPresent();

        return InvitationCheckResponse.builder()
                .planId(invitation.getPlan().getId())
                .planTitle(invitation.getPlan().getTitle())
                .role(invitation.getRole().toString())
                .inviteeEmail(invitation.getInviteeEmail())
                .userExists(userExists)
                .redirectType(userExists ? "login" : "register")
                .build();
    }

    /**
     * 사용자의 초대 목록 조회
     */
    public List<PlanInvitationResponse> getPendingInvitations(String email) {
        List<PlanInvitation> invitations = planInvitationRepository.findPendingInvitationsByEmail(email);
        return invitations.stream()
                .map(invitation -> convertToResponse(invitation, null))
                .collect(Collectors.toList());
    }

    /**
     * 플랜의 초대 목록 조회
     */
    public List<PlanInvitationResponse> getPlanInvitations(Long planId, Long userId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        // 권한 확인 (OWNER 또는 EDITOR만 조회 가능)
        validateInviterPermission(plan, userId);

        List<PlanInvitation> invitations = planInvitationRepository.findByPlanIdOrderByCreatedAtDesc(planId);
        return invitations.stream()
                .map(invitation -> convertToResponse(invitation, null))
                .collect(Collectors.toList());
    }

    // === Private Methods ===

    private void validateInviterPermission(Plan plan, Long userId) {
        boolean hasPermission = plan.getPlanMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(userId) && 
                         (member.getRole() == PlanMemberRole.OWNER || member.getRole() == PlanMemberRole.EDITOR));
        
        if (!hasPermission) {
            throw new RuntimeException("초대 권한이 없습니다. OWNER 또는 EDITOR만 초대할 수 있습니다.");
        }
    }

    private void validateNotAlreadyMember(Long planId, String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            boolean isAlreadyMember = planMemberRepository.existsByPlanIdAndUserId(planId, user.get().getId());
            if (isAlreadyMember) {
                log.warn("이미 플랜 멤버인 사용자 초대 시도: planId={}, email={}, userId={}", 
                        planId, email, user.get().getId());
                throw new RuntimeException("이미 해당 플랜의 멤버입니다.");
            }
        }
        log.debug("멤버 검증 통과: planId={}, email={}, userExists={}", 
                planId, email, user.isPresent());
    }

    private void validateNoPendingInvitation(Long planId, String email) {
        Optional<PlanInvitation> existingInvitation = planInvitationRepository.findByPlanIdAndInviteeEmail(planId, email);
        if (existingInvitation.isPresent()) {
            PlanInvitation invitation = existingInvitation.get();
            
            // 기존 초대가 있는 경우 상태와 관계없이 삭제하고 새로 초대
            log.info("기존 초대 삭제 후 재전송: planId={}, email={}, oldStatus={}, isValid={}", 
                    planId, email, invitation.getStatus(), invitation.isValid());
            planInvitationRepository.delete(invitation);
        }
    }

    private void validateInvitation(PlanInvitation invitation) {
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("이미 처리된 초대입니다.");
        }
        
        if (invitation.isExpired()) {
            throw new RuntimeException("만료된 초대 링크입니다.");
        }
    }

    private String generateInvitationToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private PlanInvitationResponse convertToResponse(PlanInvitation invitation, String inviterName) {
        return PlanInvitationResponse.builder()
                .id(invitation.getId())
                .planId(invitation.getPlan().getId())
                .planTitle(invitation.getPlan().getTitle())
                .inviteeEmail(invitation.getInviteeEmail())
                .inviterName(inviterName)
                .role(invitation.getRole())
                .status(invitation.getStatus())
                .token(invitation.getToken())
                .createdAt(invitation.getCreatedAt())
                .expiresAt(invitation.getExpiresAt())
                .build();
    }
}
