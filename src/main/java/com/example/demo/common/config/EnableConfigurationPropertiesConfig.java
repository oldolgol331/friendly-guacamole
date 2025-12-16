package com.example.demo.common.config;

import com.example.demo.common.mail.properties.EmailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * PackageName : com.example.demo.common.config
 * FileName    : EnableConfigurationPropertiesConfig
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@Configuration
@EnableConfigurationProperties(EmailProperties.class)
public class EnableConfigurationPropertiesConfig {
}
