package com.example.demo.common.mail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * PackageName : com.example.demo.common.mail.service
 * FileName    : EmailSender
 * Author      : oldolgol331
 * Date        : 26. 1. 22.
 * Description : 비동기 메일 발송 컴포넌트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 1. 22.    oldolgol331          Initial creation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender {

    private final JavaMailSender mailSender;

    /**
     * 이메일 발송을 시도하고 실패하면 재시도를 시도합니다. 3번 시도 후에 실패하면 예외를 던집니다.
     *
     * @param toEmail  - 이메일 주소
     * @param subject  - 메일 제목
     * @param htmlBody - HTML 형식 본문
     */
    @Async
    @Retryable(value = MailException.class, maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 2.0))
    public void sendHtmlEmailWithRetry(final String toEmail, final String subject, final String htmlBody) {
        log.info("이메일 발송 시도: to={}", toEmail);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(toEmail);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalArgumentException("이메일 발송 중 오류가 발생했습니다", e);
        }
    }

    /**
     * sendHtmlEmailWithRetry() 메서드(파라미터 시그니처 일치)의 모든 재시도가 실패하면 호출됩니다.
     *
     * @param e       - MailException 예외
     * @param toEmail - 이메일 주소
     * @param subject - 메일 제목
     * @param body    - 메일 본문
     */
    @Recover
    public void recover(final MailException e, final String toEmail, final String subject, final String body) {
        // 1. 상세 로그 기록
        log.error("이메일 발송에 최종 실패했습니다.(재시도 3회 모두 실패) to={}, subject={}, error={}", toEmail, subject, e.getMessage());

        // 2. 실패 알림
        // TODO: 모니터링 시스템(Prometheus, Grafana 등)에 에러 메트릭 전송 또는 알림 채널(Slack 등)로 메시지 발송 로직 추가

        // 3. Dead Letter Queue 패턴 (중요 시스템인 경우)
        // TODO: 최종 실패한 메일 발송 정보를 DB 테이블(ex. failed_emails)이나 별도 파일에 저장하여 수동 처리/분석할 수 있도록 구현
        // saveToFailedEmailQueue(toEmail, subject, body, e.getMessage());
    }

}
