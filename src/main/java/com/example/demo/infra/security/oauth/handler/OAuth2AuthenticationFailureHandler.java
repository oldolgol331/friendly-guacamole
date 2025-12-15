package com.example.demo.infra.security.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * PackageName : com.example.demo.infra.security.oauth.handler
 * FileName    : OAuth2AuthenticationFailureHandler
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : OAuth2 인증 실패 핸들러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        getRedirectStrategy().sendRedirect(request,
                                           response,
                                           UriComponentsBuilder.fromUriString("/api/v1/signin")
                                                               .queryParam("error", exception.getLocalizedMessage())
                                                               .build()
                                                               .toUriString());
    }

}
