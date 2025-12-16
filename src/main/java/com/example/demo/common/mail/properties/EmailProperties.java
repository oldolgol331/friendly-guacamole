package com.example.demo.common.mail.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/**
 * PackageName : com.example.demo.common.mail.properties
 * FileName    : EmailProperties
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 이메일(SMTP) 설정
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@ConfigurationProperties(prefix = "email")
@Getter
public class EmailProperties {

    private final long   verificationTokenExpiryMinutes;
    private final String verificationBaseUrl;
    private final long   passwordResetTokenExpiryMinutes;
    private final String passwordResetBaseUrl;

    @ConstructorBinding
    public EmailProperties(final long verificationTokenExpiryMinutes,
                           final String verificationBaseUrl,
                           final long passwordResetTokenExpiryMinutes,
                           final String passwordResetBaseUrl) {
        this.verificationTokenExpiryMinutes = verificationTokenExpiryMinutes;
        this.verificationBaseUrl = verificationBaseUrl;
        this.passwordResetTokenExpiryMinutes = passwordResetTokenExpiryMinutes;
        this.passwordResetBaseUrl = passwordResetBaseUrl;
    }

}
