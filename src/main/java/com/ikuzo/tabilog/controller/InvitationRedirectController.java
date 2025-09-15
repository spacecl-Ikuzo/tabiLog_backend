package com.ikuzo.tabilog.controller;

import com.ikuzo.tabilog.domain.invitation.PlanInvitation;
import com.ikuzo.tabilog.domain.invitation.PlanInvitationRepository;
import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 초대 링크 클릭 시 처리
     * 사용자 등록 상태에 따라 회원가입 또는 로그인 페이지로 리다이렉트
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
                return "redirect:" + frontendUrl + "/invitation/expired";
            }

            // 해당 이메일로 가입된 사용자가 있는지 확인
            Optional<User> userOpt = userRepository.findByEmail(invitation.getInviteeEmail());

            if (userOpt.isPresent()) {
                // 이미 가입된 사용자 -> 로그인 페이지로 리다이렉트 (토큰 포함)
                log.info("기존 사용자 초대 수락 요청: email={}, token={}", invitation.getInviteeEmail(), token);
                return "redirect:" + frontendUrl + "/login?invitation=" + token + "&email=" + invitation.getInviteeEmail();
            } else {
                // 새로운 사용자 -> 회원가입 페이지로 리다이렉트 (토큰과 이메일 포함)
                log.info("신규 사용자 초대 수락 요청: email={}, token={}", invitation.getInviteeEmail(), token);
                return "redirect:" + frontendUrl + "/signup?invitation=" + token + "&email=" + invitation.getInviteeEmail();
            }

        } catch (Exception e) {
            log.error("초대 처리 중 오류 발생: token={}, error={}", token, e.getMessage());
            return "redirect:" + frontendUrl + "/invitation/error";
        }
    }
}
