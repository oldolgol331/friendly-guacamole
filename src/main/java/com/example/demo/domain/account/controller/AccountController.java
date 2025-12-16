package com.example.demo.domain.account.controller;

import static com.example.demo.common.response.SuccessCode.ACCOUNT_INFO_FETCH_SUCCESS;
import static com.example.demo.common.response.SuccessCode.ACCOUNT_REGISTER_SUCCESS;
import static com.example.demo.common.response.SuccessCode.ACCOUNT_WITHDRAWN_SUCCESS;
import static com.example.demo.common.response.SuccessCode.EMAIL_SENT;
import static com.example.demo.common.response.SuccessCode.EMAIL_VERIFICATION_SUCCESS;
import static com.example.demo.common.response.SuccessCode.PASSWORD_CHANGED_SUCCESS;
import static com.example.demo.common.response.SuccessCode.UPDATE_ACCOUNT_INFO_SUCCESS;
import static com.example.demo.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_HEADER_NAME;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.SuccessCode;
import com.example.demo.common.security.model.CustomUserDetails;
import com.example.demo.common.security.service.AuthService;
import com.example.demo.domain.account.dto.AccountRequest.AccountPasswordUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountSignUpRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountWithdrawRequest;
import com.example.demo.domain.account.dto.AccountRequest.PasswordResetConfirmRequest;
import com.example.demo.domain.account.dto.AccountRequest.PasswordResetRequest;
import com.example.demo.domain.account.dto.AccountRequest.ResendVerificationEmailRequest;
import com.example.demo.domain.account.dto.AccountResponse.AccountInfoResponse;
import com.example.demo.domain.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.example.demo.domain.account.controller
 * FileName    : AccountController
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 계정(Account) 컨트롤러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "계정 API", description = "계정 가입, 정보 조회/수정, 탈퇴 API를 제공합니다.")
public class AccountController {

    private final AccountService accountService;
    private final AuthService    authService;

    @PostMapping
    @Operation(summary = "회원 가입", description = "아이디(이메일)와 비밀번호로 계정 가입을 합니다.")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody final AccountSignUpRequest request
    ) {
        accountService.signUpEmailUser(request);
        final SuccessCode successCode = ACCOUNT_REGISTER_SUCCESS;
        return ResponseEntity.status(successCode.getStatus()).body(ApiResponse.success(successCode));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "이메일 인증", description = "발송된 메일에서 인증 토큰을 검증하고, 인증을 완료합니다.(유효 시간 내)")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam final String token) {
        accountService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(EMAIL_VERIFICATION_SUCCESS));
    }

    @PostMapping("/verify-email-resend")
    @Operation(summary = "인증 메일 재발송", description = "입력받은 이베일 주소로 인증 메일을 재발송합니다.(1분 내 재요청 금지)")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(
            @Valid @RequestBody final ResendVerificationEmailRequest request
    ) {
        accountService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(EMAIL_SENT));
    }

    @GetMapping
    @Operation(summary = "계정 정보 조회", description = "로그인된 계정의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<AccountInfoResponse>> getMyInfo(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        AccountInfoResponse responseData = accountService.getAccountInfoById(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(ACCOUNT_INFO_FETCH_SUCCESS, responseData));
    }

    @PutMapping
    @Operation(summary = "계정 정보 수정", description = "로그인된 계정의 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<AccountInfoResponse>> updateMyInfo(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @Valid @RequestBody final AccountUpdateRequest request
    ) {
        AccountInfoResponse responseData = accountService.updateAccountInfo(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(UPDATE_ACCOUNT_INFO_SUCCESS, responseData));
    }

    @PostMapping("/password-reset-request")
    @Operation(summary = "비밀번호 재설정 요청", description = "가입된 이메일로 비밀번호 재설정 링크를 발송합니다.")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody final PasswordResetRequest request
    ) {
        accountService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(EMAIL_SENT));
    }

    @PostMapping("/password-reset-confirm")
    @Operation(summary = "비밀번호 재설정 확인 및 변경", description = "이메일로 받은 토큰을 검증하고 새로운 비밀번호로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @RequestParam final String token,
            @Valid @RequestBody final PasswordResetConfirmRequest request
    ) {
        accountService.confirmPasswordReset(token, request);
        return ResponseEntity.ok(ApiResponse.success(PASSWORD_CHANGED_SUCCESS));
    }

    @PatchMapping
    @Operation(summary = "비밀번호 변경", description = "로그인된 계정의 비밀번호를 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestHeader(JWT_ACCESS_TOKEN_HEADER_NAME) final String accessToken,
            @Valid @RequestBody final AccountPasswordUpdateRequest request
    ) {
        accountService.changePassword(userDetails.getId(), request);
        authService.signout(userDetails, accessToken.substring(7));
        return ResponseEntity.ok(ApiResponse.success(PASSWORD_CHANGED_SUCCESS));
    }

    @DeleteMapping
    @Operation(summary = "이메일 계정 탈퇴", description = "로그인된 이메일 계정의 상태를 탈퇴로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @Valid @RequestBody(required = false) final AccountWithdrawRequest request,
            @RequestHeader(JWT_ACCESS_TOKEN_HEADER_NAME) final String accessToken
    ) {
        accountService.withdrawAccount(userDetails.getId(), request);
        authService.signout(userDetails, accessToken.substring(7));
        return ResponseEntity.ok(ApiResponse.success(ACCOUNT_WITHDRAWN_SUCCESS));
    }

    @DeleteMapping("/oauth")
    @Operation(summary = "OAuth 계정 탈퇴", description = "로그인된 OAuth 계정의 상태를 탈퇴로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> withdrawOAuth(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestHeader(JWT_ACCESS_TOKEN_HEADER_NAME) final String accessToken
    ) {
        accountService.withdrawAccount(userDetails.getId());
        authService.signout(userDetails, accessToken.substring(7));
        return ResponseEntity.ok(ApiResponse.success(ACCOUNT_WITHDRAWN_SUCCESS));
    }

}
