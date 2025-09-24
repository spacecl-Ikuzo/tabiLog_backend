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
        log.info("이메일 전송 시작 - From: {}, To: {}, Inviter: {}, Plan: {}", fromEmail, toEmail, inviterName, planTitle);
        
        try {
            // 이메일 주소 유효성 검사
            if (toEmail == null || toEmail.trim().isEmpty() || !toEmail.contains("@")) {
                throw new IllegalArgumentException("유효하지 않은 이메일 주소입니다: " + toEmail);
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[TabiLog] " + inviterName + "님이 여행 계획에 초대했습니다");

            // HTML 템플릿 생성 (해시 라우터 형식)
            String invitationUrl = frontendUrl + "/#/invitation/" + invitationToken;
            log.debug("초대 URL 생성: {}", invitationUrl);
            
            String htmlContent = createInvitationEmailTemplate(inviterName, planTitle, invitationUrl);
            helper.setText(htmlContent, true);

            log.info("SMTP 서버로 이메일 전송 중...");
            mailSender.send(message);
            log.info("✅ 플랜 초대 이메일 전송 완료: {} -> {}", fromEmail, toEmail);

        } catch (MessagingException e) {
            log.error("❌ 플랜 초대 이메일 전송 실패 (MessagingException): {}", e.getMessage(), e);
            throw new RuntimeException("이메일 전송에 실패했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ 플랜 초대 이메일 전송 실패 (Exception): {}", e.getMessage(), e);
            throw new RuntimeException("이메일 전송 중 예상치 못한 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 와리깡 정보 이메일 전송
     */
    public void sendWarikanEmail(String toEmail, String senderName, String planTitle, 
                                String warikanTitle, Long totalAmount, Long memberAmount, 
                                String frontendUrl, String memberName, Long planId) {
        log.info("와리깡 이메일 전송 시작 - From: {}, To: {}, Sender: {}, Plan: {}", 
                fromEmail, toEmail, senderName, planTitle);
        
        try {
            // 이메일 주소 유효성 검사
            if (toEmail == null || toEmail.trim().isEmpty() || !toEmail.contains("@")) {
                throw new IllegalArgumentException("유효하지 않은 이메일 주소입니다: " + toEmail);
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[TabiLog] " + senderName + "さんから割り勘のお知らせです");

            // HTML 템플릿 생성
            String planUrl = frontendUrl + "/#/plans/" + planId;
            log.debug("플랜 URL 생성: {}", planUrl);
            
            String htmlContent = createWarikanEmailTemplate(senderName, planTitle, warikanTitle, 
                    totalAmount, memberAmount, planUrl, memberName);
            helper.setText(htmlContent, true);

            log.info("SMTP 서버로 와리깡 이메일 전송 중...");
            mailSender.send(message);
            log.info("✅ 와리깡 이메일 전송 완료: {} -> {}", fromEmail, toEmail);

        } catch (MessagingException e) {
            log.error("❌ 와리깡 이메일 전송 실패 (MessagingException): {}", e.getMessage(), e);
            throw new RuntimeException("이메일 전송에 실패했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ 와리깡 이메일 전송 실패 (Exception): {}", e.getMessage(), e);
            throw new RuntimeException("이메일 전송 중 예상치 못한 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 이메일 서비스 상태 확인 (테스트용)
     */
    public boolean testEmailConnection() {
        try {
            log.info("이메일 연결 테스트 시작 - SMTP 서버: smtp.gmail.com:587");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(fromEmail); // 자기 자신에게 테스트 이메일
            helper.setSubject("[TabiLog] 이메일 연결 테스트");
            helper.setText("이메일 서비스가 정상적으로 작동합니다.", false);
            
            mailSender.send(message);
            log.info("✅ 이메일 연결 테스트 성공");
            return true;
        } catch (Exception e) {
            log.error("❌ 이메일 연결 테스트 실패: {}", e.getMessage(), e);
            return false;
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
                "<p>아래 버튼을 클릭하여 초대를 수락하고 여행 계획에 참여해보세요!</p>" +
                "<div class=\"btn-container\">" +
                "<a href=\"" + invitationUrl + "\" class=\"btn\">초대 수락하기</a>" +
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

    /**
     * 와리깡 이메일 템플릿 생성 (일본어)
     */
    private String createWarikanEmailTemplate(String senderName, String planTitle, String warikanTitle, 
                                            Long totalAmount, Long memberAmount, String planUrl, 
                                            String memberName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>TabiLog 割り勘のお知らせ</title>" +
                "<style>" +
                "body { font-family: 'Hiragino Sans', 'Yu Gothic', 'Meiryo', sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #ff6b6b 0%, #ffa500 100%); color: white; padding: 30px; text-align: center; }" +
                ".content { padding: 30px; }" +
                ".title { font-size: 24px; font-weight: bold; margin-bottom: 10px; }" +
                ".subtitle { font-size: 16px; opacity: 0.9; }" +
                ".warikan-info { background-color: #fff8f0; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ff6b6b; }" +
                ".warikan-title { font-size: 20px; font-weight: bold; color: #333; margin-bottom: 15px; }" +
                ".amount-section { background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0; }" +
                ".total-amount { font-size: 18px; font-weight: bold; color: #333; margin-bottom: 10px; }" +
                ".member-amount { font-size: 16px; color: #666; }" +
                ".description { background-color: #e8f4fd; padding: 15px; border-radius: 5px; margin: 15px 0; color: #333; }" +
                ".btn-container { text-align: center; margin: 30px 0; }" +
                ".btn { display: inline-block; padding: 15px 30px; background: linear-gradient(135deg, #ff6b6b 0%, #ffa500 100%); color: white; text-decoration: none; border-radius: 25px; font-weight: bold; transition: transform 0.2s; }" +
                ".btn:hover { transform: translateY(-2px); }" +
                ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }" +
                ".note { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; color: #856404; }" +
                ".sender-info { color: #666; font-size: 14px; margin-bottom: 10px; }" +
                ".member-profile { background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0; }" +
                ".member-name { font-weight: bold; color: #333; margin-bottom: 5px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<div class=\"title\">💰 TabiLog</div>" +
                "<div class=\"subtitle\">割り勘のお知らせ</div>" +
                "</div>" +
                "<div class=\"content\">" +
                "<div class=\"sender-info\">👤 " + senderName + "さんから</div>" +
                "<div class=\"warikan-info\">" +
                "<div class=\"warikan-title\">📋 " + warikanTitle + "</div>" +
                "<div style=\"color: #666; margin-bottom: 15px;\">プラン: " + planTitle + "</div>" +
                "</div>" +
                "<div class=\"member-profile\">" +
                "<div class=\"member-name\">👤 " + memberName + "さん</div>" +
                "<div style=\"color: #666; font-size: 14px;\">この割り勘の対象者です</div>" +
                "</div>" +
                "<div class=\"amount-section\">" +
                "<div class=\"total-amount\">💰 トータル費用: " + String.format("%,d", totalAmount) + "円</div>" +
                "<div class=\"member-amount\">💳 あなたの負担額: " + String.format("%,d", memberAmount) + "円</div>" +
                "</div>" +
                "<div class=\"btn-container\">" +
                "<a href=\"" + planUrl + "\" class=\"btn\">プランを確認する</a>" +
                "</div>" +
                "<div class=\"note\">" +
                "<strong>📝 ご案内:</strong><br>" +
                "• この割り勘はTabiLogのプランに関連しています<br>" +
                "• 上記の金額をご確認の上、精算をお願いします<br>" +
                "• ご不明な点がございましたら、プラン作成者にお問い合わせください" +
                "</div>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>このメールはTabiLogから自動送信されました。</p>" +
                "<p>ご質問がございましたら、お気軽にお問い合わせください。</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
