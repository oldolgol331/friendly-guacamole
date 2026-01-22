package com.example.demo.common.mail.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
 * FileName    : EmailSenderTest
 * Author      : oldolgol331
 * Date        : 26. 1. 22.
 * Description : EmailSender 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 1. 22.    oldolgol331          Initial creation
 */
@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

    @InjectMocks
    private EmailSender emailSender;

    @Mock
    private JavaMailSender mailSender;

    @Nested
    @DisplayName("sendHtmlEmailWithRetry() 테스트")
    class SendHtmlEmailWithRetryTest {

        @Test
        @DisplayName("이메일 발송 성공")
        void sendHtmlEmailWithRetry_Success() throws Exception {
            // given
            String toEmail  = "test@example.com";
            String subject  = "Test Subject";
            String htmlBody = "<html><body>Test Body</body></html>";

            MimeMessage mimeMessage = mock(MimeMessage.class);

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            // when
            emailSender.sendHtmlEmailWithRetry(toEmail, subject, htmlBody);

            // then
            verify(mailSender, times(1)).createMimeMessage();
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("이메일 발송 실패")
        void sendHtmlEmailWithRetry_Failure() throws Exception {
            // given
            String toEmail  = "test@example.com";
            String subject  = "Test Subject";
            String htmlBody = "<html><body>Test Body</body></html>";

            MimeMessage mimeMessage = mock(MimeMessage.class);

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new IllegalArgumentException()).when(mailSender).send(any(MimeMessage.class));

            // when & then
            assertThrows(IllegalArgumentException.class,
                         () -> emailSender.sendHtmlEmailWithRetry(toEmail, subject, htmlBody));

            verify(mailSender, times(1)).createMimeMessage();
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }

    }

}
