package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.domain.invitation.PlanInvitation;
import com.ikuzo.tabilog.domain.invitation.PlanInvitationRepository;
import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserRepository;
import com.ikuzo.tabilog.service.PlanInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/invitation")
@RequiredArgsConstructor
public class InvitationRedirectController {

    private final PlanInvitationRepository planInvitationRepository;
    private final UserRepository userRepository;
    private final PlanInvitationService planInvitationService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 초대 링크 클릭 시 처리 - 바로 초대 수락 처리
     * 로그인된 상태면 바로 플랜 멤버로 추가, 아니면 로그인/회원가입 페이지로 이동
     */
    @GetMapping("/{token}")
    public String handleInvitationLink(@PathVariable String token) {
        try {
            // 토큰으로 초대 조회
            Optional<PlanInvitation> invitationOpt = planInvitationRepository.findByToken(token);
            
            if (invitationOpt.isEmpty()) {
                log.warn("유효하지 않은 초대 토큰: {}", token);
                return "redirect:" + frontendUrl + "/invitation/invalid";
            }

            PlanInvitation invitation = invitationOpt.get();

            // 초대가 만료되었거나 이미 처리된 경우
            if (!invitation.isValid()) {
                log.warn("만료되거나 처리된 초대: token={}, status={}, expiresAt={}", 
                        token, invitation.getStatus(), invitation.getExpiresAt());
                
                // 이미 수락된 초대인 경우 플랜 페이지로 바로 이동
                if (invitation.getStatus().toString().equals("ACCEPTED")) {
                    Long planId = invitation.getPlan().getId();
                    log.info("이미 수락된 초대 - 플랜으로 이동: planId={}, token={}", planId, token);
                    return "redirect:" + frontendUrl + "/plans/" + planId;
                }
                
                return "redirect:" + frontendUrl + "/invitation/expired";
            }

            // 현재 로그인된 사용자 확인
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                // 로그인된 상태 - 바로 초대 수락 처리 시도
                try {
                    // 로그인된 사용자 정보 가져오기
                    String currentUserEmail = auth.getName();
                    Optional<User> currentUserOpt = userRepository.findByEmail(currentUserEmail);
                    
                    if (currentUserOpt.isPresent()) {
                        User currentUser = currentUserOpt.get();
                        
                        // 초대된 이메일과 현재 로그인된 이메일이 일치하는지 확인
                        if (currentUser.getEmail().equals(invitation.getInviteeEmail())) {
                            // 바로 초대 수락 처리
                           String redirectUrl = planInvitationService.acceptInvitation(token, currentUser.getId(), true);
                            log.info("로그인된 사용자 초대 자동 수락 완료: email={}, planId={}", 
                                    currentUser.getEmail(), invitation.getPlan().getId());
                            return "redirect:" + frontendUrl + redirectUrl;
                        } else {
                            // 다른 계정으로 로그인된 경우
                            log.warn("초대된 이메일({})과 로그인된 이메일({})이 다름", 
                                    invitation.getInviteeEmail(), currentUser.getEmail());
                            return "redirect:" + frontendUrl + "/invitation/email-mismatch?invited=" + 
                                   invitation.getInviteeEmail() + "&current=" + currentUser.getEmail();
                        }
                    }
                } catch (Exception e) {
                    log.error("로그인된 사용자 초대 수락 처리 실패: {}", e.getMessage());
                    // 실패 시 일반 플로우로 진행
                }
            }

            // 로그인되지 않은 상태 또는 자동 수락 실패 시 invitation 페이지로 리다이렉트
            // 프론트엔드에서 API 호출로 사용자 상태 확인 후 로그인/회원가입 페이지로 리다이렉트 처리
            log.info("초대 페이지로 리다이렉트: email={}, token={}, planTitle={}", 
                    invitation.getInviteeEmail(), token, invitation.getPlan().getTitle());
            return "redirect:" + frontendUrl + "/invitation/" + token;

        } catch (Exception e) {
            log.error("초대 처리 중 오류 발생: token={}, error={}", token, e.getMessage());
            return "redirect:" + frontendUrl + "/invitation/error";
        }
    }
}
