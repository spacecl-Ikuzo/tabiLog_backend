package com.ikuzo.tabilog.service;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

            // HTML 템플릿 생성
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
     * 이메일 인증코드 전송 후 생성된 코드를 반환
     */
    public String sendVerificationCode(String toEmail) {
        log.info("이메일 인증코드 전송 시작 - To: {}", toEmail);
        try {
            if (toEmail == null || toEmail.trim().isEmpty() || !toEmail.contains("@")) {
                throw new IllegalArgumentException("유효하지 않은 이메일 주소입니다: " + toEmail);
            }

            String code = generateSixDigitCode();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[TabiLog] 이메일 인증코드");

            String html = createVerificationEmailTemplate(code);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("✅ 인증코드 이메일 전송 완료: {} -> {}", fromEmail, toEmail);
            return code;
        } catch (MessagingException e) {
            log.error("❌ 인증코드 이메일 전송 실패 (MessagingException): {}", e.getMessage(), e);
            throw new RuntimeException("인증코드 이메일 전송에 실패했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ 인증코드 이메일 전송 실패 (Exception): {}", e.getMessage(), e);
            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private String generateSixDigitCode() {
        SecureRandom random = new SecureRandom();
        int value = random.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    private String createVerificationEmailTemplate(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>TabiLog 이메일 인증</title>" +
                "<style>" +
                "body { font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }" +
                ".content { padding: 30px; }" +
                ".title { font-size: 24px; font-weight: bold; margin-bottom: 10px; }" +
                ".subtitle { font-size: 16px; opacity: 0.9; }" +
                ".code { font-size: 32px; font-weight: bold; letter-spacing: 6px; background-color: #f8f9fa; padding: 15px 20px; border-radius: 8px; text-align: center; }" +
                ".note { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; color: #856404; }" +
                ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<div class=\"title\">🧳 TabiLog 이메일 인증</div>" +
                "<div class=\"subtitle\">아래 인증코드를 입력해주세요</div>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>안녕하세요! 아래 6자리 인증코드를 입력하여 이메일 인증을 완료해주세요.</p>" +
                "<div class=\"code\">" + code + "</div>" +
                "<div class=\"note\"><strong>유효시간:</strong> 이 코드는 10분 후 만료됩니다.</div>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>이 이메일은 TabiLog에서 자동으로 발송되었습니다.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
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
     * 비밀번호 재설정 이메일 전송
     */
    public void sendPasswordResetEmail(String toEmail, String nickname, String resetUrl) {
        log.info("비밀번호 재설정 이메일 전송 시작 - To: {}", toEmail);
        
        try {
            // 이메일 주소 유효성 검사
            if (toEmail == null || toEmail.trim().isEmpty() || !toEmail.contains("@")) {
                throw new IllegalArgumentException("유효하지 않은 이메일 주소입니다: " + toEmail);
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[TabiLog] 비밀번호 재설정");

            // HTML 템플릿 생성
            String htmlContent = createPasswordResetEmailTemplate(nickname, resetUrl);
            helper.setText(htmlContent, true);

            log.info("SMTP 서버로 비밀번호 재설정 이메일 전송 중...");
            mailSender.send(message);
            log.info("✅ 비밀번호 재설정 이메일 전송 완료: {} -> {}", fromEmail, toEmail);

        } catch (MessagingException e) {
            log.error("❌ 비밀번호 재설정 이메일 전송 실패 (MessagingException): {}", e.getMessage(), e);
            throw new RuntimeException("비밀번호 재설정 이메일 전송에 실패했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ 비밀번호 재설정 이메일 전송 실패 (Exception): {}", e.getMessage(), e);
            throw new RuntimeException("이메일 전송 중 예상치 못한 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 비밀번호 재설정 이메일 템플릿 생성
     */
    private String createPasswordResetEmailTemplate(String nickname, String resetUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>TabiLog 비밀번호 재설정</title>" +
                "<style>" +
                "body { font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }" +
                ".content { padding: 30px; }" +
                ".title { font-size: 24px; font-weight: bold; margin-bottom: 10px; }" +
                ".subtitle { font-size: 16px; opacity: 0.9; }" +
                ".btn-container { text-align: center; margin: 30px 0; }" +
                ".btn { display: inline-block; padding: 15px 30px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; border-radius: 25px; font-weight: bold; transition: transform 0.2s; }" +
                ".btn:hover { transform: translateY(-2px); }" +
                ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }" +
                ".note { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; color: #856404; }" +
                ".warning { background-color: #f8d7da; border: 1px solid #f5c6cb; padding: 15px; border-radius: 5px; margin: 20px 0; color: #721c24; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<div class=\"title\">🔐 TabiLog 비밀번호 재설정</div>" +
                "<div class=\"subtitle\">새로운 비밀번호를 설정해주세요</div>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>안녕하세요, <strong>" + nickname + "</strong>님!</p>" +
                "<p>비밀번호 재설정 요청을 받았습니다. 아래 버튼을 클릭하여 새로운 비밀번호를 설정해주세요.</p>" +
                "<div class=\"btn-container\">" +
                "<a href=\"" + resetUrl + "\" class=\"btn\">비밀번호 재설정하기</a>" +
                "</div>" +
                "<div class=\"note\">" +
                "<strong>📝 안내사항:</strong><br>" +
                "• 이 링크는 30분 후 만료됩니다<br>" +
                "• 보안을 위해 한 번만 사용할 수 있습니다<br>" +
                "• 본인이 요청하지 않은 경우 이 이메일을 무시해주세요" +
                "</div>" +
                "<div class=\"warning\">" +
                "<strong>⚠️ 보안 경고:</strong><br>" +
                "• 이 링크를 다른 사람과 공유하지 마세요<br>" +
                "• 의심스러운 활동이 있다면 즉시 고객센터로 연락해주세요" +
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
