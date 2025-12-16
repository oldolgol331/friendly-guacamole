package com.example.demo.domain.account.controller;

import static com.example.demo.common.response.SuccessCode.ACCOUNT_LOGIN_SUCCESS;
import static com.example.demo.common.response.SuccessCode.ACCOUNT_LOGOUT_SUCCESS;
import static com.example.demo.common.response.SuccessCode.AUTHENTICATION_TOKEN_RENEW_SUCCESS;
import static com.example.demo.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_HEADER_NAME;
import static com.example.demo.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_PREFIX;
import static com.example.demo.common.security.constant.SecurityConst.JWT_REFRESH_TOKEN_COOKIE_NAME;
import static com.example.demo.domain.account.model.AccountRole.USER;
import static com.example.demo.domain.account.model.AccountStatus.ACTIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.common.config.AppConfig;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.advice.PageResponseAdvice;
import com.example.demo.common.security.annotation.CustomWithMockUser;
import com.example.demo.common.security.config.SecurityConfig;
import com.example.demo.common.security.config.TestSecurityConfig;
import com.example.demo.common.security.jwt.filter.JwtAuthenticationFilter;
import com.example.demo.common.security.jwt.provider.JwtProvider;
import com.example.demo.common.security.model.CustomUserDetails;
import com.example.demo.common.security.service.AuthService;
import com.example.demo.common.util.TestUtils;
import com.example.demo.domain.account.dto.AccountRequest.AccountSignInRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * PackageName : com.example.demo.domain.account.controller
 * FileName    : AuthControllerTest
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : AuthController 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@WebMvcTest(value = AuthController.class,
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE,
                                     classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@AutoConfigureWebMvc
@Import({AppConfig.class, PageResponseAdvice.class, TestSecurityConfig.class})
class AuthControllerTest {

    @Autowired
    MockMvc      mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    AuthService  authService;
    @MockitoBean
    JwtProvider  jwtProvider;

    @RepeatedTest(10)
    @DisplayName("로그인")
    void signin() throws Exception {
        // given
        AccountSignInRequest request     = TestUtils.createAccountSignInRequest();
        String               requestBody = objectMapper.writeValueAsString(request);
        CustomUserDetails userDetails = CustomUserDetails.of(UUID.randomUUID(),
                                                             request.getEmail(),
                                                             request.getPassword(),
                                                             USER,
                                                             ACTIVE);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,
                                                                                null,
                                                                                userDetails.getAuthorities());
        String accessToken            = UUID.randomUUID().toString().replace("-", "");
        String refreshToken           = UUID.randomUUID().toString().replace("-", "");
        long   refreshTokenExpiration = 1209600L;

        when(authService.signin(eq(request.getEmail()), eq(request.getPassword()))).thenReturn(authentication);
        when(jwtProvider.generateAccessToken(eq(authentication))).thenReturn(accessToken);
        when(jwtProvider.generateRefreshToken(eq(authentication))).thenReturn(refreshToken);
        when(jwtProvider.getRefreshTokenExpirationSeconds()).thenReturn(refreshTokenExpiration);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/signin")
                                                              .contentType(APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        // then
        ApiResponse<Object> apiResponse  = ApiResponse.success(ACCOUNT_LOGIN_SUCCESS);
        String              responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AuthController.class))
                     .andExpect(handler().methodName("signin"))
                     .andExpect(status().isOk())
                     .andExpect(header().string(JWT_ACCESS_TOKEN_HEADER_NAME,
                                                JWT_ACCESS_TOKEN_PREFIX + accessToken))
                     .andExpect(cookie().value(JWT_REFRESH_TOKEN_COOKIE_NAME, refreshToken))
                     .andExpect(cookie().httpOnly(JWT_REFRESH_TOKEN_COOKIE_NAME, true))
                     .andExpect(cookie().secure(JWT_REFRESH_TOKEN_COOKIE_NAME, true))
                     .andExpect(cookie().path(JWT_REFRESH_TOKEN_COOKIE_NAME, "/"))
                     .andExpect(cookie().maxAge(JWT_REFRESH_TOKEN_COOKIE_NAME,
                                                (int) refreshTokenExpiration))
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @RepeatedTest(10)
    @CustomWithMockUser
    @DisplayName("로그아웃")
    void signout() throws Exception {
        // given
        String rawAccessToken    = UUID.randomUUID().toString().replace("-", "");
        String bearerAccessToken = JWT_ACCESS_TOKEN_PREFIX + rawAccessToken;

        doNothing().when(authService).signout(any(CustomUserDetails.class), eq(rawAccessToken));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/signout")
                                                              .header(AUTHORIZATION, bearerAccessToken));

        // then
        ApiResponse<Void> apiResponse  = ApiResponse.success(ACCOUNT_LOGOUT_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AuthController.class))
                     .andExpect(handler().methodName("signout"))
                     .andExpect(status().isOk())
                     .andExpect(cookie().path(JWT_REFRESH_TOKEN_COOKIE_NAME, "/"))
                     .andExpect(cookie().maxAge(JWT_REFRESH_TOKEN_COOKIE_NAME, 0))
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @RepeatedTest(10)
    @DisplayName("AccessToken 갱신")
    void refresh() throws Exception {
        // given
        String refreshToken       = UUID.randomUUID().toString().replace("-", "");
        String newAccessToken     = UUID.randomUUID().toString().replace("-", "");
        Cookie refreshTokenCookie = new Cookie(JWT_REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        when(authService.refresh(eq(refreshToken))).thenReturn(newAccessToken);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/refresh")
                                                              .cookie(refreshTokenCookie));

        // then
        ApiResponse<Void> apiResponse  = ApiResponse.success(AUTHENTICATION_TOKEN_RENEW_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AuthController.class))
                     .andExpect(handler().methodName("refresh"))
                     .andExpect(status().isOk())
                     .andExpect(header().string(JWT_ACCESS_TOKEN_HEADER_NAME,
                                                JWT_ACCESS_TOKEN_PREFIX + newAccessToken))
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

}
