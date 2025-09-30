package com.ikuzo.tabilog.service;


import java.security.SecureRandom;
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
                "<title>TabiLog メール認証</title>" +
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
                "<div class=\"title\">🧳 TabiLog メール認証</div>" +
                "<div class=\"subtitle\">下記の認証コードを入力してください</div>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>こんにちは！下記の6桁の認証コードを入力してメール認証を完了してください。</p>" +
                "<div class=\"code\">" + code + "</div>" +
                "<div class=\"note\"><strong>有効時間：</strong>このコードは10分後に期限切れになります。</div>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>このメールはTabiLogから自動送信されました。</p>" +
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
                "<title>TabiLog 旅行計画への招待</title>" +
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
                "<div class=\"subtitle\">旅行計画に招待されました！</div>" +
                "</div>" +
                "<div class=\"content\">" +
                "<div class=\"plan-info\">" +
                "<div class=\"plan-title\">📋 " + planTitle + "</div>" +
                "<div class=\"inviter\">👤 " + inviterName + "さんが招待しました</div>" +
                "</div>" +
                "<p>こんにちは！</p>" +
                "<p><strong>" + inviterName + "</strong>さんが<strong>" + planTitle + "</strong>の旅行計画にあなたを招待しました。</p>" +
                "<p>下記のボタンをクリックして招待を承諾し、旅行計画に参加してみてください！</p>" +
                "<div class=\"btn-container\">" +
                "<a href=\"" + invitationUrl + "\" class=\"btn\">招待を承諾する</a>" +
                "</div>" +
                "<div class=\"note\">" +
                "<strong>📝 ご案内：</strong><br>" +
                "• TabiLogアカウントをお持ちでない場合は、会員登録後に自動的に旅行計画に参加できます<br>" +
                "• すでにアカウントをお持ちの場合は、ログイン後すぐに旅行計画を確認できます<br>" +
                "• この招待は7日後に期限切れになります" +
                "</div>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>このメールはTabiLogから自動送信されました。</p>" +
                "<p>ご質問がございましたら、カスタマーサービスまでお問い合わせください。</p>" +
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
                "<title>TabiLog パスワード再設定</title>" +
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
                "<div class=\"title\">🔐 TabiLog パスワード再設定</div>" +
                "<div class=\"subtitle\">新しいパスワードを設定してください</div>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>こんにちは、<strong>" + nickname + "</strong>さん！</p>" +
                "<p>パスワード再設定のリクエストを受け取りました。下記のボタンをクリックして新しいパスワードを設定してください。</p>" +
                "<div class=\"btn-container\">" +
                "<a href=\"" + resetUrl + "\" class=\"btn\">パスワード再設定</a>" +
                "</div>" +
                "<div class=\"note\">" +
                "<strong>📝 ご案内：</strong><br>" +
                "• このリンクは30分後に期限切れになります<br>" +
                "• セキュリティのため、一度だけ使用できます<br>" +
                "• ご自身でリクエストしていない場合は、このメールを無視してください" +
                "</div>" +
                "<div class=\"warning\">" +
                "<strong>⚠️ セキュリティ警告：</strong><br>" +
                "• このリンクを他の人と共有しないでください<br>" +
                "• 疑わしい活動がある場合は、すぐにカスタマーサービスまでご連絡ください" +
                "</div>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>このメールはTabiLogから自動送信されました。</p>" +
                "<p>ご質問がございましたら、カスタマーサービスまでお問い合わせください。</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    
}
