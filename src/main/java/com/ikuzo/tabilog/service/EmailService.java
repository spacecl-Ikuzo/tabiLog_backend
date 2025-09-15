package com.ikuzo.tabilog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 플랜 초대 이메일 전송
     */
    public void sendPlanInvitationEmail(String toEmail, String inviterName, String planTitle, String invitationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[TabiLog] " + inviterName + "님이 여행 계획에 초대했습니다");

            // HTML 템플릿 생성
            String invitationUrl = frontendUrl + "/invitation/" + invitationToken;
            String htmlContent = createInvitationEmailTemplate(inviterName, planTitle, invitationUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("플랜 초대 이메일 전송 완료: {} -> {}", fromEmail, toEmail);

        } catch (MessagingException e) {
            log.error("플랜 초대 이메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    /**
     * 이메일 템플릿 생성 (간단한 HTML)
     */
    private String createInvitationEmailTemplate(String inviterName, String planTitle, String invitationUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>TabiLog 여행 계획 초대</title>" +
                "<style>" +
                "body { font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }" +
                ".content { padding: 30px; }" +
                ".title { font-size: 24px; font-weight: bold; margin-bottom: 10px; }" +
                ".subtitle { font-size: 16px; opacity: 0.9; }" +
                ".plan-info { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                ".plan-title { font-size: 18px; font-weight: bold; color: #333; margin-bottom: 10px; }" +
                ".inviter { color: #666; }" +
                ".btn-container { text-align: center; margin: 30px 0; }" +
                ".btn { display: inline-block; padding: 15px 30px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; border-radius: 25px; font-weight: bold; transition: transform 0.2s; }" +
                ".btn:hover { transform: translateY(-2px); }" +
                ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }" +
                ".note { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; color: #856404; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<div class=\"title\">🧳 TabiLog</div>" +
                "<div class=\"subtitle\">여행 계획에 초대되었습니다!</div>" +
                "</div>" +
                "<div class=\"content\">" +
                "<div class=\"plan-info\">" +
                "<div class=\"plan-title\">📋 " + planTitle + "</div>" +
                "<div class=\"inviter\">👤 " + inviterName + "님이 초대했습니다</div>" +
                "</div>" +
                "<p>안녕하세요!</p>" +
                "<p><strong>" + inviterName + "</strong>님이 <strong>" + planTitle + "</strong> 여행 계획에 당신을 초대했습니다.</p>" +
                "<p>아래 버튼을 클릭하여 초대를 확인하고 여행 계획에 참여해보세요!</p>" +
                "<div class=\"btn-container\">" +
                "<a href=\"" + invitationUrl + "\" class=\"btn\">초대 확인하기</a>" +
                "</div>" +
                "<div class=\"note\">" +
                "<strong>📝 안내사항:</strong><br>" +
                "• TabiLog 계정이 없으시면 회원가입 후 자동으로 여행 계획에 참여됩니다<br>" +
                "• 이미 계정이 있으시면 로그인 후 바로 여행 계획을 확인할 수 있습니다<br>" +
                "• 이 초대는 7일 후 만료됩니다" +
                "</div>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>이 이메일은 TabiLog에서 자동으로 발송되었습니다.</p>" +
                "<p>문의사항이 있으시면 고객센터로 연락해주세요.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
