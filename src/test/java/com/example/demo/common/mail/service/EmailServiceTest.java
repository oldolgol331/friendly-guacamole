package com.example.demo.common.mail.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * PackageName : com.example.demo.common.mail.service
 * FileName    : EmailServiceTest
 * Author      : oldolgol331
 * Date        : 25. 12. 24.
 * Description : EmailServiceImpl 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 24.   oldolgol331          Initial creation
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @Mock
    private EmailSender emailSender;

    @Nested
    @DisplayName("sendVerificationEmail() 테스트")
    class SendVerificationEmailTests {

        @Test
        @DisplayName("이메일 인증 링크 전송 성공")
        void sendVerificationEmail_Success() throws Exception {
            // given
            String toEmail          = "test@example.com";
            String verificationLink = "http://example.com/verify?token=abc123";

            doNothing().when(emailSender).sendHtmlEmailWithRetry(eq(toEmail), eq("[ticket] 회원가입 이메일 인증"), anyString());

            // when
            emailService.sendVerificationEmail(toEmail, verificationLink);

            // then
            verify(emailSender, times(1)).sendHtmlEmailWithRetry(eq(toEmail), eq("[ticket] 회원가입 이메일 인증"), anyString());
        }

    }

    @Nested
    @DisplayName("sendPasswordResetEmail() 테스트")
    class SendPasswordResetEmailTests {

        @Test
        @DisplayName("비밀번호 재설정 링크 전송 성공")
        void sendPasswordResetEmail_Success() throws Exception {
            // given
            String toEmail   = "test@example.com";
            String resetLink = "http://example.com/reset?token=abc123";

            doNothing().when(emailSender)
                       .sendHtmlEmailWithRetry(eq(toEmail), eq("[ticket] 비밀번호 재설정 요청 안내"), anyString());

            // when
            emailService.sendPasswordResetEmail(toEmail, resetLink);

            // then
            verify(emailSender, times(1)).sendHtmlEmailWithRetry(
                    eq(toEmail), eq("[ticket] 비밀번호 재설정 요청 안내"), anyString()
            );
        }

    }

}