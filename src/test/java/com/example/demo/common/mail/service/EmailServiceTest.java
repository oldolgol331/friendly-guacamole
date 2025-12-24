package com.example.demo.common.mail.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

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
    private JavaMailSender mailSender;

    @Nested
    @DisplayName("sendVerificationEmail() 테스트")
    class SendVerificationEmailTests {

        @Test
        @DisplayName("이메일 인증 링크 전송 성공")
        void sendVerificationEmail_Success() throws Exception {
            // given
            String toEmail = "test@example.com";
            String verificationLink = "http://example.com/verify?token=abc123";
            MimeMessage mimeMessage = mock(MimeMessage.class);

            // mailSender가 MimeMessage를 생성하도록 설정
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            // when
            emailService.sendVerificationEmail(toEmail, verificationLink);

            // then
            verify(mailSender, times(1)).createMimeMessage();
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }

    }

    @Nested
    @DisplayName("sendPasswordResetEmail() 테스트")
    class SendPasswordResetEmailTests {

        @Test
        @DisplayName("비밀번호 재설정 링크 전송 성공")
        void sendPasswordResetEmail_Success() throws Exception {
            // given
            String toEmail = "test@example.com";
            String resetLink = "http://example.com/reset?token=abc123";
            MimeMessage mimeMessage = mock(MimeMessage.class);

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            // when
            emailService.sendPasswordResetEmail(toEmail, resetLink);

            // then
            verify(mailSender, times(1)).createMimeMessage();
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }

    }

    @Nested
    @DisplayName("sendHtmlEmailWithRetry() 테스트")
    class SendHtmlEmailWithRetryTests {

        @Test
        @DisplayName("HTML 이메일 발송 성공")
        void sendHtmlEmailWithRetry_Success() throws Exception {
            // given
            String toEmail = "test@example.com";
            String subject = "Test Subject";
            String htmlBody = "<html><body>Test Body</body></html>";
            MimeMessage mimeMessage = mock(MimeMessage.class);

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            // when
            emailService.sendHtmlEmailWithRetry(toEmail, subject, htmlBody);

            // then
            verify(mailSender, times(1)).createMimeMessage();
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }

    }

}