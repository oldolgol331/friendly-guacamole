package com.example.demo.infra.security.oauth.handler;

import static com.example.demo.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_HEADER_NAME;
import static com.example.demo.common.security.constant.SecurityConst.JWT_REFRESH_TOKEN_COOKIE_NAME;

import com.example.demo.common.security.jwt.provider.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * PackageName : com.example.demo.infra.security.oauth.handler
 * FileName    : OAuth2AuthenticationSuccessHandler
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : OAuth2 인증 성공 핸들러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String accessToken  = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        response.setHeader(JWT_ACCESS_TOKEN_HEADER_NAME, accessToken);

        long refreshTokenExpirationSeconds = jwtProvider.getRefreshTokenExpirationSeconds();

        ResponseCookie cookie = ResponseCookie.from(JWT_REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                                              .httpOnly(true)
                                              .secure(true)
                                              .path("/")
                                              .maxAge(refreshTokenExpirationSeconds)
                                              .sameSite("Lax")
                                              .build();
        response.addHeader("Set-Cookie", cookie.toString());

        getRedirectStrategy().sendRedirect(request,
                                           response,
                                           UriComponentsBuilder.fromUriString("/api/v1/signin")
                                                               .build()
                                                               .toUriString());
    }

}
