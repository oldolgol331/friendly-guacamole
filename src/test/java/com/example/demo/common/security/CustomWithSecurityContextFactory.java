package com.example.demo.common.security;

import com.example.demo.common.security.annotation.CustomWithMockUser;
import com.example.demo.common.security.model.CustomUserDetails;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * PackageName : com.example.demo.common.security
 * FileName    : CustomWithSecurityContextFactory
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 테스트용 인증(Authentication) 컨텍스트 팩토리
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
public class CustomWithSecurityContextFactory implements WithSecurityContextFactory<CustomWithMockUser> {

    @Override
    public SecurityContext createSecurityContext(CustomWithMockUser annotation) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        CustomUserDetails userDetails = CustomUserDetails.of(UUID.fromString(annotation.id()),
                                                             annotation.email(),
                                                             annotation.password(),
                                                             annotation.role(),
                                                             annotation.status());
        securityContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        return securityContext;
    }

}
