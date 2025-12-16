package com.example.demo.domain.account.controller;

import static com.example.demo.common.response.SuccessCode.ACCOUNT_LOGIN_SUCCESS;
import static com.example.demo.common.response.SuccessCode.ACCOUNT_LOGOUT_SUCCESS;
import static com.example.demo.common.response.SuccessCode.AUTHENTICATION_TOKEN_RENEW_SUCCESS;
import static com.example.demo.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_HEADER_NAME;
import static com.example.demo.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_PREFIX;
import static com.example.demo.common.security.constant.SecurityConst.JWT_REFRESH_TOKEN_COOKIE_NAME;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.security.jwt.provider.JwtProvider;
import com.example.demo.common.security.model.CustomUserDetails;
import com.example.demo.common.security.service.AuthService;
import com.example.demo.domain.account.dto.AccountRequest.AccountSignInRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.example.demo.domain.account.controller
 * FileName    : AuthController
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 인증(Auth) 컨트롤러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "로그인, 로그아웃, 토큰 갱신 API를 제공합니다.")
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signin")
    @Operation(summary = "로그인", description = "아이디(이메일)와 비밀번호를 사용하여 로그인합니다.")
    public ResponseEntity<ApiResponse<Void>> signin(@Valid @RequestBody final AccountSignInRequest request,
                                                    HttpServletResponse response) {
        Authentication authentication = authService.signin(request.getEmail(), request.getPassword());

        String accessToken  = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        long refreshTokenExpirationSeconds = jwtProvider.getRefreshTokenExpirationSeconds();

        response.setHeader(JWT_ACCESS_TOKEN_HEADER_NAME, JWT_ACCESS_TOKEN_PREFIX + accessToken);

        ResponseCookie cookie = ResponseCookie.from(JWT_REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                                              .httpOnly(true)
                                              .secure(true)
                                              .path("/")
                                              .maxAge(refreshTokenExpirationSeconds)
                                              .sameSite("Lax")
                                              .build();
        response.setHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(ApiResponse.success(ACCOUNT_LOGIN_SUCCESS));
    }

    @PostMapping("/signout")
    @Operation(summary = "로그아웃", description = "로그인된 상태에서 로그아웃합니다.")
    public ResponseEntity<ApiResponse<Void>> signout(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestHeader(JWT_ACCESS_TOKEN_HEADER_NAME) final String accessToken,
            HttpServletResponse response
    ) {
        authService.signout(userDetails, accessToken.substring(7));

        ResponseCookie cookie = ResponseCookie.from(JWT_REFRESH_TOKEN_COOKIE_NAME, null)
                                              .path("/")
                                              .maxAge(0)
                                              .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(ApiResponse.success(ACCOUNT_LOGOUT_SUCCESS));
    }

    @PostMapping("/refresh")
    @Operation(summary = "AccessToken 갱신", description = "RefreshToken을 사용하여 AccessToken을 갱신합니다.")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @CookieValue(JWT_REFRESH_TOKEN_COOKIE_NAME) final String refreshToken,
            HttpServletResponse response
    ) {
        String newAccessToken = authService.refresh(refreshToken);
        response.setHeader(JWT_ACCESS_TOKEN_HEADER_NAME, JWT_ACCESS_TOKEN_PREFIX + newAccessToken);
        return ResponseEntity.ok(ApiResponse.success(AUTHENTICATION_TOKEN_RENEW_SUCCESS));
    }

}
