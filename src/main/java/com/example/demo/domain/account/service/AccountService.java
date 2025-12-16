package com.example.demo.domain.account.service;

import com.example.demo.domain.account.dto.AccountRequest.AccountPasswordUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountSignUpRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountWithdrawRequest;
import com.example.demo.domain.account.dto.AccountRequest.PasswordResetConfirmRequest;
import com.example.demo.domain.account.dto.AccountResponse.AccountInfoResponse;
import com.example.demo.domain.account.model.Account;
import java.util.UUID;

/**
 * PackageName : com.example.demo.domain.account.service
 * FileName    : AccountService
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 계정(Account) 서비스 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
public interface AccountService {

    AccountInfoResponse signUpEmailUser(AccountSignUpRequest request);

    Account findOrCreateAccountForOAuth(String provider,
                                        String providerId,
                                        String emailFromOAuth,
                                        String nicknameFromOAuth);

    void resendVerificationEmail(String email);

    AccountInfoResponse verifyEmail(String token);

    AccountInfoResponse getAccountInfoById(UUID accountId);

    AccountInfoResponse updateAccountInfo(UUID accountId, AccountUpdateRequest request);

    void sendPasswordResetEmail(String email);

    void confirmPasswordReset(String token, PasswordResetConfirmRequest request);

    void changePassword(UUID accountId, AccountPasswordUpdateRequest request);

    void withdrawAccount(UUID accountId);

    void withdrawAccount(UUID accountId, AccountWithdrawRequest request);

}
