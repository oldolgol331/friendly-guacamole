package com.example.demo.common.mail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * PackageName : com.example.demo.common.mail.service
 * FileName    : EmailServiceImpl
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 이메일 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailSender emailSender;

    /**
     * 회원가입 이메일 인증 메일을 발송합니다.
     *
     * @param toEmail          - 이메일 주소
     * @param verificationLink - 인증 링크
     */
    @Override
    public void sendVerificationEmail(final String toEmail, final String verificationLink) {
        final String subject  = "[ticket] 회원가입 이메일 인증";
        final String htmlBody = createEmailVerificationHtml(verificationLink);
        emailSender.sendHtmlEmailWithRetry(toEmail, subject, htmlBody);
    }

    /**
     * 비밀번호 초기화 메일을 발송합니다.
     *
     * @param toEmail   - 이메일 주소
     * @param resetLink - 비밀번호 초기화 링크
     */
    @Override
    public void sendPasswordResetEmail(final String toEmail, final String resetLink) {
        final String subject  = "[ticket] 비밀번호 재설정 요청 안내";
        final String htmlBody = createPasswordResetHtml(resetLink);
        emailSender.sendHtmlEmailWithRetry(toEmail, subject, htmlBody);
    }

    //========================= 내부 메서드 =========================

    /**
     * 이메일 인증 본문(HTML)을 생성합니다.
     *
     * @param verificationLink - 인증 링크
     * @return HTML 형식 문자열
     */
    private String createEmailVerificationHtml(final String verificationLink) {
        return "<!DOCTYPE html>"
               + "<html lang='ko'>"
               + "<head>"
               + "<meta charset='UTF-8'>"
               + "<style>"
               + "body { font-family: 'Apple SD Gothic Neo', 'sans-serif'; text-align: center; background-color: "
               + "#f4f4f4; padding: 40px; }"
               + ".container { background-color: #ffffff; max-width: 600px; margin: 0 auto; padding: 30px; "
               + "border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }"
               + "h1 { color: #333333; }"
               + "p { color: #555555; font-size: 16px; line-height: 1.5; }"
               + ".button { display: inline-block; background-color: #007bff; color: #ffffff; padding: 15px 25px; "
               + "text-decoration: none; border-radius: 5px; font-weight: bold; margin-top: 20px; }"
               + ".footer { margin-top: 30px; font-size: 12px; color: #aaaaaa; }"
               + "</style>"
               + "</head>"
               + "<body>"
               + "<div class='container'>"
               + "<h1>이메일 주소를 인증해주세요.</h1>"
               + "<p>저희 서비스를 이용해주셔서 감사합니다.<br>회원가입을 완료하려면 아래 버튼을 클릭하여 이메일 주소를 인증해주세요.</p>"
               + "<a href='" + verificationLink + "' class='button'>이메일 인증하기</a>"
               + "<p class='footer'>이 링크는 10분 동안 유효합니다.<br>만약 직접 요청한 것이 아니라면 이 이메일을 무시해주세요.</p>"
               + "</div>"
               + "</body>"
               + "</html>";
    }

    /**
     * 비밀번호 초기화 본문(HTML)을 생성합니다.
     *
     * @param resetLink 비밀번호 초기화 링크
     * @return HTML 형식 문자열
     */
    private String createPasswordResetHtml(final String resetLink) {
        return "<!DOCTYPE html>"
               + "<html lang='ko'>"
               + "<head>"
               + "<meta charset='UTF-8'>"
               + "<style>" // 스타일은 공통화 가능
               + "body { font-family: 'Apple SD Gothic Neo', 'sans-serif'; text-align: center; background-color: "
               + "#f4f4f4; padding: 40px; }"
               + ".container { background-color: #ffffff; max-width: 600px; margin: 0 auto; padding: 30px; "
               + "border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }"
               + "h1 { color: #333333; }"
               + "p { color: #555555; font-size: 16px; line-height: 1.5; }"
               + ".button { display: inline-block; background-color: #ffc107; color: #000000; padding: 15px 25px; "
               + "text-decoration: none; border-radius: 5px; font-weight: bold; margin-top: 20px; }"
               + ".footer { margin-top: 30px; font-size: 12px; color: #aaaaaa; }"
               + "</style>"
               + "</head>"
               + "<body>"
               + "<div class='container'>"
               + "<h1>비밀번호 재설정 요청</h1>"
               + "<p>비밀번호 재설정을 요청하셨습니다.<br>새로운 비밀번호를 설정하려면 아래 버튼을 클릭해주세요.</p>"
               + "<a href='" + resetLink + "' class='button'>비밀번호 재설정하기</a>"
               + "<p class='footer'>이 링크는 30분 동안 유효합니다.<br>만약 직접 요청한 것이 아니라면 이 이메일을 무시해주세요.</p>"
               + "</div>"
               + "</body>"
               + "</html>";
    }

}
