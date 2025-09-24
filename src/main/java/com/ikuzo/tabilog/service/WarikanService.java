package com.ikuzo.tabilog.service;

import com.ikuzo.tabilog.domain.plan.Plan;
import com.ikuzo.tabilog.domain.plan.PlanMember;
import com.ikuzo.tabilog.domain.plan.PlanMemberRepository;
import com.ikuzo.tabilog.domain.plan.PlanRepository;
import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserRepository;
import com.ikuzo.tabilog.dto.request.WarikanRequest;
import com.ikuzo.tabilog.dto.response.WarikanResponse;
import com.ikuzo.tabilog.exception.PlanNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarikanService {

    private final PlanRepository planRepository;
    private final PlanMemberRepository planMemberRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 와리깡 정보를 플랜 멤버들에게 이메일로 전송
     */
    @Transactional
    public WarikanResponse sendWarikanToMembers(WarikanRequest request, Long senderId) {
        // 플랜 조회
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException(request.getPlanId()));

        // 요청자가 플랜 멤버인지 확인
        validatePlanMember(request.getPlanId(), senderId);

        // 발신자 정보 조회
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("발신자를 찾을 수 없습니다: " + senderId));

        // 플랜 멤버 목록 조회 (발신자 포함)
        List<PlanMember> members = planMemberRepository.findByPlanId(request.getPlanId());

        log.info("플랜 {}의 전체 멤버 수: {}, 발신자 ID: {}", 
                request.getPlanId(), members.size(), senderId);
        
        // 요청된 사용자 ID들 로깅
        List<Long> requestedUserIds = request.getMemberShares().stream()
                .map(WarikanRequest.MemberShare::getUserId)
                .collect(Collectors.toList());
        log.info("요청된 사용자 ID들: {}", requestedUserIds);
        
        // 각 멤버에게 개별 이메일 전송
        for (WarikanRequest.MemberShare memberShare : request.getMemberShares()) {
            log.info("사용자 ID {} 찾는 중...", memberShare.getUserId());
            
            PlanMember member = members.stream()
                    .filter(m -> m.getUser().getId().equals(memberShare.getUserId()))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("멤버를 찾을 수 없습니다. 사용자 ID: {}, 플랜 ID: {}, 사용 가능한 멤버들: {}", 
                                memberShare.getUserId(), request.getPlanId(), 
                                members.stream().map(m -> m.getUser().getId()).collect(Collectors.toList()));
                        return new RuntimeException("멤버를 찾을 수 없습니다: " + memberShare.getUserId());
                    });

            try {
                emailService.sendWarikanEmail(
                        member.getUser().getEmail(),
                        sender.getNickname(),
                        plan.getTitle(),
                        request.getTitle(),
                        request.getTotalAmount(),
                        memberShare.getAmount(),
                        frontendUrl,
                        member.getUser().getNickname(),
                        member.getUser().getProfileImageUrl()
                );
                log.info("와리깡 이메일 전송 완료: {} -> {}", sender.getNickname(), member.getUser().getEmail());
            } catch (Exception e) {
                log.error("와리깡 이메일 전송 실패: {} -> {}, error: {}", 
                        sender.getNickname(), member.getUser().getEmail(), e.getMessage());
                // 개별 이메일 실패해도 전체 프로세스는 계속 진행
            }
        }

        // 응답 생성
        List<WarikanResponse.MemberShareResponse> memberShareResponses = request.getMemberShares().stream()
                .map(share -> {
                    PlanMember member = members.stream()
                            .filter(m -> m.getUser().getId().equals(share.getUserId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (member == null) return null;
                    
                    return WarikanResponse.MemberShareResponse.builder()
                            .userId(member.getUser().getId())
                            .memberName(member.getUser().getNickname())
                            .memberEmail(member.getUser().getEmail())
                            .amount(share.getAmount())
                            .profileImageUrl(member.getUser().getProfileImageUrl())
                            .build();
                })
                .filter(share -> share != null)
                .collect(Collectors.toList());

        return WarikanResponse.builder()
                .planId(plan.getId())
                .title(request.getTitle())
                .totalAmount(request.getTotalAmount())
                .senderName(sender.getNickname())
                .senderEmail(sender.getEmail())
                .memberShares(memberShareResponses)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 플랜 멤버 권한 확인
     */
    private void validatePlanMember(Long planId, Long userId) {
        boolean isMember = planMemberRepository.existsByPlanIdAndUserId(planId, userId);
        if (!isMember) {
            throw new RuntimeException("플랜에 접근할 권한이 없습니다.");
        }
    }
}
