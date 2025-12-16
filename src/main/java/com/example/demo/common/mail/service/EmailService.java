package com.example.demo.common.mail.service;

/**
 * PackageName : com.example.demo.common.mail.service
 * FileName    : EmailService
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 이메일 서비스 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
public interface EmailService {

    void sendVerificationEmail(String toEmail, String verificationLink);

    void sendPasswordResetEmail(String toEmail, String resetLink);

}
