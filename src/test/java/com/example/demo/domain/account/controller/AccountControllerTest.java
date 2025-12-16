package com.example.demo.domain.account.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.common.config.AppConfig;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.SuccessCode;
import com.example.demo.common.response.advice.PageResponseAdvice;
import com.example.demo.common.security.annotation.CustomWithMockUser;
import com.example.demo.common.security.config.SecurityConfig;
import com.example.demo.common.security.config.TestSecurityConfig;
import com.example.demo.common.security.constant.SecurityConst;
import com.example.demo.common.security.jwt.filter.JwtAuthenticationFilter;
import com.example.demo.common.security.model.CustomUserDetails;
import com.example.demo.common.security.service.AuthService;
import com.example.demo.common.util.TestUtils;
import com.example.demo.domain.account.dto.AccountRequest.AccountPasswordUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountSignUpRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountWithdrawRequest;
import com.example.demo.domain.account.dto.AccountRequest.PasswordResetConfirmRequest;
import com.example.demo.domain.account.dto.AccountRequest.PasswordResetRequest;
import com.example.demo.domain.account.dto.AccountRequest.ResendVerificationEmailRequest;
import com.example.demo.domain.account.dto.AccountResponse.AccountInfoResponse;
import com.example.demo.domain.account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * PackageName : com.example.demo.domain.account.controller
 * FileName    : AccountControllerTest
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : AccountController 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@WebMvcTest(value = AccountController.class,
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE,
                                     classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@AutoConfigureWebMvc
@Import({AppConfig.class, PageResponseAdvice.class, TestSecurityConfig.class})
class AccountControllerTest {

    @Autowired
    MockMvc        mockMvc;
    @Autowired
    ObjectMapper   objectMapper;
    @MockitoBean
    AccountService accountService;
    @MockitoBean
    AuthService    authService;

    @RepeatedTest(10)
    @DisplayName("회원 가입")
    void signup() throws Exception {
        // given
        AccountSignUpRequest request     = TestUtils.createAccountSignUpRequest();
        String               requestBody = objectMapper.writeValueAsString(request);

        AccountInfoResponse responseData = TestUtils.createAccountInfoResponse();

        when(accountService.signUpEmailUser(eq(request))).thenReturn(responseData);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/accounts")
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        // then
        ApiResponse<Void> apiResponse  = ApiResponse.success(SuccessCode.ACCOUNT_REGISTER_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AccountController.class))
                     .andExpect(handler().methodName("signup"))
                     .andExpect(status().isCreated())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(accountService, times(1)).signUpEmailUser(eq(request));
    }

    @RepeatedTest(10)
    @DisplayName("이메일 인증")
    void verifyEmail() throws Exception {
        //Given
        String token = UUID.randomUUID().toString().replace("-", "");

        AccountInfoResponse responseData = TestUtils.createAccountInfoResponse();

        when(accountService.verifyEmail(eq(token))).thenReturn(responseData);

        //When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/accounts/verify-email")
                                                              .param("token", token));

        //Then
        ApiResponse<Void> apiResponse  = ApiResponse.success(SuccessCode.EMAIL_VERIFICATION_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AccountController.class))
                     .andExpect(handler().methodName("verifyEmail"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(accountService, times(1)).verifyEmail(eq(token));
    }

    @RepeatedTest(10)
    @DisplayName("인증 메일 재발송")
    void resendVerificationEmail() throws Exception {
        //Given
        ResendVerificationEmailRequest request     = new ResendVerificationEmailRequest("test@example.com");
        String                         requestBody = objectMapper.writeValueAsString(request);

        doNothing().when(accountService).resendVerificationEmail(eq(request.getEmail()));

        //When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/accounts/verify-email-resend")
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        ApiResponse<Void> apiResponse  = ApiResponse.success(SuccessCode.EMAIL_SENT);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AccountController.class))
                     .andExpect(handler().methodName("resendVerificationEmail"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(accountService, times(1)).resendVerificationEmail(eq(request.getEmail()));
    }

    @RepeatedTest(10)
    @CustomWithMockUser
    @DisplayName("회원 정보 조회")
    void getMyInfo() throws Exception {
        //Given
        AccountInfoResponse responseData = TestUtils.createAccountInfoResponse();

        when(accountService.getAccountInfoById(any(UUID.class))).thenReturn(responseData);

        //When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/accounts"));

        //Then
        ApiResponse<AccountInfoResponse> apiResponse = ApiResponse.success(SuccessCode.ACCOUNT_INFO_FETCH_SUCCESS,
                                                                           responseData);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AccountController.class))
                     .andExpect(handler().methodName("getMyInfo"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(accountService, times(1)).getAccountInfoById(any(UUID.class));
    }

    @RepeatedTest(10)
    @CustomWithMockUser
    @DisplayName("회원 정보 수정")
    void updateMyInfo() throws Exception {
        //Given
        AccountUpdateRequest request     = TestUtils.createAccountUpdateRequest("Test12!@");
        String               requestBody = objectMapper.writeValueAsString(request);

        AccountInfoResponse responseData = TestUtils.createAccountInfoResponse();

        when(accountService.updateAccountInfo(any(UUID.class), eq(request))).thenReturn(responseData);

        //When
        ResultActions resultActions = mockMvc.perform(put("/api/v1/accounts")
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        ApiResponse<AccountInfoResponse> apiResponse = ApiResponse.success(SuccessCode.UPDATE_ACCOUNT_INFO_SUCCESS,
                                                                           responseData);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AccountController.class))
                     .andExpect(handler().methodName("updateMyInfo"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(accountService, times(1)).updateAccountInfo(any(UUID.class), eq(request));
    }

    @RepeatedTest(10)
    @DisplayName("비밀번호 재설정 요청")
    void requestPasswordReset() throws Exception {
        // given
        PasswordResetRequest request     = new PasswordResetRequest("test@example.com");
        String               requestBody = objectMapper.writeValueAsString(request);

        doNothing().when(accountService).sendPasswordResetEmail(eq(request.getEmail()));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/accounts/password-reset-request")
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        // then
        ApiResponse<Void> apiResponse  = ApiResponse.success(SuccessCode.EMAIL_SENT);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AccountController.class))
                     .andExpect(handler().methodName("requestPasswordReset"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(accountService, times(1)).sendPasswordResetEmail(eq(request.getEmail()));
    }

    @RepeatedTest(10)
    @DisplayName("비밀번호 재설정 확인 및 변경")
    void confirmPasswordReset() throws Exception {
        // given
        PasswordResetConfirmRequest request     = TestUtils.createPasswordResetConfirmRequest();
        String                      requestBody = objectMapper.writeValueAsString(request);

        String token = UUID.randomUUID().toString().replace("-", "");

        doNothing().when(accountService).confirmPasswordReset(eq(token), eq(request));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/accounts/password-reset-confirm")
                                                              .param("token", token)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        // then
        ApiResponse<Void> apiResponse  = ApiResponse.success(SuccessCode.PASSWORD_CHANGED_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AccountController.class))
                     .andExpect(handler().methodName("confirmPasswordReset"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(accountService, times(1)).confirmPasswordReset(eq(token), eq(request));
    }

    @RepeatedTest(10)
    @CustomWithMockUser
    @DisplayName("비밀번호 변경")
    void changePassword() throws Exception {
        //Given
        AccountPasswordUpdateRequest request     = TestUtils.createAccountPasswordUpdateRequest("Test12!@");
        String                       requestBody = objectMapper.writeValueAsString(request);

        String rawAccessToken    = UUID.randomUUID().toString().replace("-", "");
        String bearerAccessToken = SecurityConst.JWT_ACCESS_TOKEN_PREFIX + rawAccessToken;

        doNothing().when(accountService).changePassword(any(UUID.class), eq(request));
        doNothing().when(authService).signout(any(CustomUserDetails.class), eq(rawAccessToken));

        //When
        ResultActions resultActions = mockMvc.perform(patch("/api/v1/accounts")
                                                              .header(HttpHeaders.AUTHORIZATION, bearerAccessToken)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        ApiResponse<Void> apiResponse  = ApiResponse.success(SuccessCode.PASSWORD_CHANGED_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AccountController.class))
                     .andExpect(handler().methodName("changePassword"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(accountService, times(1)).changePassword(any(UUID.class), eq(request));
    }

    @RepeatedTest(10)
    @CustomWithMockUser
    @DisplayName("이메일 회원 탈퇴")
    void withdraw() throws Exception {
        //Given
        AccountWithdrawRequest request     = TestUtils.createAccountWithdrawRequest("Test12!@");
        String                 requestBody = objectMapper.writeValueAsString(request);

        String rawAccessToken    = UUID.randomUUID().toString().replace("-", "");
        String bearerAccessToken = SecurityConst.JWT_ACCESS_TOKEN_PREFIX + rawAccessToken;

        doNothing().when(accountService).withdrawAccount(any(UUID.class), eq(request));
        doNothing().when(authService).signout(any(CustomUserDetails.class), eq(rawAccessToken));

        //When
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/accounts")
                                                              .header(HttpHeaders.AUTHORIZATION, bearerAccessToken)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        ApiResponse<Void> apiResponse  = ApiResponse.success(SuccessCode.ACCOUNT_WITHDRAWN_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AccountController.class))
                     .andExpect(handler().methodName("withdraw"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(accountService, times(1)).withdrawAccount(any(UUID.class), eq(request));
    }

    @RepeatedTest(10)
    @CustomWithMockUser
    @DisplayName("OAuth 회원 탈퇴")
    void withdrawOAuth() throws Exception {
        //Given
        String rawAccessToken    = UUID.randomUUID().toString().replace("-", "");
        String bearerAccessToken = SecurityConst.JWT_ACCESS_TOKEN_PREFIX + rawAccessToken;

        doNothing().when(accountService).withdrawAccount(any(UUID.class));
        doNothing().when(authService).signout(any(CustomUserDetails.class), eq(rawAccessToken));

        //When
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/accounts/oauth")
                                                              .header(HttpHeaders.AUTHORIZATION, bearerAccessToken));

        //Then
        ApiResponse<Void> apiResponse  = ApiResponse.success(SuccessCode.ACCOUNT_WITHDRAWN_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(AccountController.class))
                     .andExpect(handler().methodName("withdrawOAuth"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(accountService, times(1)).withdrawAccount(any(UUID.class));
    }

}
