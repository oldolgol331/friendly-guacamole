package com.example.demo.domain.account.service;

import static com.example.demo.common.response.ErrorCode.ACCOUNT_ALREADY_WITHDRAWN;
import static com.example.demo.common.response.ErrorCode.ACCOUNT_BLOCKED;
import static com.example.demo.common.response.ErrorCode.ACCOUNT_INACTIVE;
import static com.example.demo.common.response.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.example.demo.common.response.ErrorCode.ALREADY_VERIFIED_EMAIL;
import static com.example.demo.common.response.ErrorCode.EMAIL_DUPLICATION;
import static com.example.demo.common.response.ErrorCode.INVALID_PASSWORD;
import static com.example.demo.common.response.ErrorCode.INVALID_VERIFICATION_TOKEN;
import static com.example.demo.common.response.ErrorCode.NICKNAME_DUPLICATION;
import static com.example.demo.common.response.ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED;
import static com.example.demo.common.response.ErrorCode.OAUTH_USER_CANNOT_RESET_PASSWORD;
import static com.example.demo.common.response.ErrorCode.PASSWORD_MISMATCH;
import static com.example.demo.common.response.ErrorCode.TOO_MANY_REQUESTS;
import static com.example.demo.domain.account.model.AccountStatus.ACTIVE;
import static com.example.demo.infra.redis.constant.RedisConst.REDIS_PASSWORD_RESET_KEY_PREFIX;
import static com.example.demo.infra.redis.constant.RedisConst.REDIS_PASSWORD_RESET_RATE_LIMIT_KEY_PREFIX;
import static com.example.demo.infra.redis.constant.RedisConst.REDIS_VERIFICATION_KEY_PREFIX;
import static com.example.demo.infra.redis.constant.RedisConst.REDIS_VERIFICATION_RATE_LIMIT_KEY_PREFIX;

import com.example.demo.common.error.BusinessException;
import com.example.demo.common.mail.properties.EmailProperties;
import com.example.demo.common.mail.service.EmailService;
import com.example.demo.domain.account.dao.AccountRepository;
import com.example.demo.domain.account.dao.OAuthConnectionRepository;
import com.example.demo.domain.account.dto.AccountRequest.AccountPasswordUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountSignUpRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountWithdrawRequest;
import com.example.demo.domain.account.dto.AccountRequest.PasswordResetConfirmRequest;
import com.example.demo.domain.account.dto.AccountResponse.AccountInfoResponse;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.model.OAuthConnection;
import com.example.demo.infra.redis.dao.RedisRepository;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.example.demo.domain.account.service
 * FileName    : AccountServiceImpl
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 계정(Account) 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository         accountRepository;
    private final OAuthConnectionRepository oAuthConnectionRepository;
    private final RedisRepository           redisRepository;
    private final PasswordEncoder           passwordEncoder;
    private final EmailService              emailService;
    private final EmailProperties           emailProperties;

    /**
     * 회원 가입 요청을 처리합니다. 이메일 인증을 위한 토큰을 생성하고 이메일을 발송합니다.
     *
     * @param request - 계정 가입 요청 DTO
     * @return 가입된 계정 정보 응답 DTO
     */
    @Transactional
    @Override
    public AccountInfoResponse signUpEmailUser(final AccountSignUpRequest request) {
        String lowerCaseEmail = request.getEmail().toLowerCase();

        if (!request.isPasswordConfirmed()) throw new BusinessException(PASSWORD_MISMATCH); // 비밀번호 != 비밀번호 확인
        if (accountRepository.existsByEmail(lowerCaseEmail)) throw new BusinessException(EMAIL_DUPLICATION);  // 이메일 중복
        if (accountRepository.existsByNickname(request.getNickname()))
            throw new BusinessException(NICKNAME_DUPLICATION); // 닉네임 중복

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Account account = accountRepository.save(Account.of(lowerCaseEmail, encodedPassword, request.getNickname()));

        sendVerificationEmailWithToken(account);    // 인증 메일 발송

        return AccountInfoResponse.from(account);
    }

    /**
     * OAuth 제공자로부터 계정 정보를 저장하거나, 이미 존재하는 경우 기존 계정에 OAuth 연동을 추가합니다.
     *
     * @param provider          - OAuth 제공자
     * @param providerId        - OAuth 고유 식별자
     * @param emailFromOAuth    - OAuth 제공자로부터 받은 이메일
     * @param nicknameFromOAuth - OAuth 제공자로부터 받은 닉네임
     * @return 계정 엔티티
     */
    @Transactional
    @Override
    public Account findOrCreateAccountForOAuth(final String provider,
                                               final String providerId,
                                               final String emailFromOAuth,
                                               final String nicknameFromOAuth) {
        Optional<Account> opAccount = accountRepository.findByProviderAndProviderId(provider, providerId);
        if (opAccount.isPresent()) return opAccount.get();

        if (emailFromOAuth != null && !emailFromOAuth.isBlank()) {
            Optional<Account> opAccountFromEmail = accountRepository.findByEmail(emailFromOAuth);

            // 이메일 주소로 가입된 계정이 존재하는 경우
            if (opAccountFromEmail.isPresent()) {
                Account         accountToLink   = opAccountFromEmail.get();
                OAuthConnection oAuthConnection = OAuthConnection.of(accountToLink, provider, providerId);

                switch (accountToLink.getStatus()) {
                    case INACTIVE:  // 비활성 상태인 경우
                        oAuthConnectionRepository.save(oAuthConnection);
                        accountToLink.completeEmailVerification();  // 이메일 인증 완료 처리
                        return accountToLink;
                    case ACTIVE:    // 활성 상태인 경우
                        oAuthConnectionRepository.save(oAuthConnection);
                        return accountToLink;
                    case DELETED:   // 탈퇴 상태인 경우
                        accountRepository.deleteByEmail(emailFromOAuth);    // 해당 계정 완전 삭제
                        break;
                    case BLOCKED:
                        throw new BusinessException(ACCOUNT_BLOCKED);
                }
            }
        }

        String newNickname = nicknameFromOAuth;
        if (accountRepository.existsByNickname(newNickname))
            newNickname = "user-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        Account savedAccount = accountRepository.save(Account.of(emailFromOAuth, newNickname, provider, providerId));
        oAuthConnectionRepository.save(OAuthConnection.of(savedAccount, provider, providerId));

        return savedAccount;
    }

    /**
     * 비활성화(INACTIVE) 상태의 계정에게 인증 메일을 재발송합니다. 인증 메일 재발송 요청 반복을 1분간 금지합니다.
     *
     * @param email - 이메일 주소
     */
    @Transactional
    @Override
    public void resendVerificationEmail(final String email) {
        String lowerCaseEmail = email.toLowerCase();
        String rateLimitKey   = REDIS_VERIFICATION_RATE_LIMIT_KEY_PREFIX + lowerCaseEmail;

        if (redisRepository.hasKey(rateLimitKey)) throw new BusinessException(TOO_MANY_REQUESTS);

        Account account = accountRepository.findByEmail(lowerCaseEmail)
                                           .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND));

        switch (account.getStatus()) {
            case ACTIVE -> throw new BusinessException(ALREADY_VERIFIED_EMAIL);   // 이미 인증된 계정일 경우
            case DELETED -> throw new BusinessException(ACCOUNT_ALREADY_WITHDRAWN);   // 탈퇴한 계정일 경우
            case BLOCKED -> throw new BusinessException(ACCOUNT_BLOCKED); // 차단된 계정일 경우
        }

        sendVerificationEmailWithToken(account);

        redisRepository.setValue(rateLimitKey, "sent", Duration.ofMinutes(1));  // 1분간 재요청 금지
    }

    /**
     * 이메일 인증 토큰을 검증하고, 계정 상태를 활성화(ACTIVE)로 변경합니다.
     *
     * @param token - 이메일 인증 토큰
     * @return 인증된 계정 정보 응답 DTO
     */
    @Transactional
    @Override
    public AccountInfoResponse verifyEmail(final String token) {
        String redisKey = REDIS_VERIFICATION_KEY_PREFIX + token;

        UUID accountId = UUID.fromString(
                redisRepository.getValue(redisKey, String.class)
                               .orElseThrow(() -> new BusinessException(INVALID_VERIFICATION_TOKEN))
        );

        Account account = accountRepository.findById(accountId)
                                           .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND));

        switch (account.getStatus()) {
            case ACTIVE -> throw new BusinessException(ALREADY_VERIFIED_EMAIL);   // 이미 인증된 계정일 경우
            case DELETED -> throw new BusinessException(ACCOUNT_ALREADY_WITHDRAWN);   // 탈퇴한 계정일 경우
            case BLOCKED -> throw new BusinessException(ACCOUNT_BLOCKED); // 차단된 계정일 경우
        }

        account.completeEmailVerification();
        redisRepository.deleteData(redisKey);

        return AccountInfoResponse.from(account);
    }

    /**
     * 계정 정보를 가져옵니다. 계정의 탈퇴일이 있는 경우 null을 반환합니다.
     *
     * @param accountId - 계정 ID
     * @return 조회된 계정 정보 응답 DTO
     */
    @Override
    public AccountInfoResponse getAccountInfoById(final UUID accountId) {
//        Account account = accountRepository.findById(accountId)
//                                           .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));
//        accountStatusCheck(account);
//        return AccountInfoResponse.from(account);
        return accountRepository.getAccountInfoResponseByIdAndStatus(accountId, ACTIVE)
                                .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND));
    }

    /**
     * 계정 정보를 수정합니다. 비밀번호가 일치하는지 확인하고, 닉네임 중복 확인을 합니다.
     *
     * @param accountId - 계정 ID
     * @param request   - 계정 정보 수정 요청 DTO
     * @return 수정된 계정 정보 응답 DTO
     */
    @Transactional
    @Override
    public AccountInfoResponse updateAccountInfo(final UUID accountId, final AccountUpdateRequest request) {
        Account account = accountRepository.findById(accountId)
                                           .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND));
        accountStatusCheck(account);

        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword()))  // 비밀번호 일치 여부 확인
            throw new BusinessException(INVALID_PASSWORD);
        if (!account.getNickname().equals(request.getNewNickname())
            && accountRepository.existsByNickname(request.getNewNickname()))    // 신규 닉네임이 이미 사용 중인 경우
            throw new BusinessException(NICKNAME_DUPLICATION);

        account.setNickname(request.getNewNickname());

        return AccountInfoResponse.from(account);
    }

    /**
     * 비밀번호 재설정 이메일을 발송합니다.
     *
     * @param email - 이메일 주소
     */
    @Override
    public void sendPasswordResetEmail(final String email) {
        String lowerCaseEmail = email.toLowerCase();
        String rateLimitKey   = REDIS_PASSWORD_RESET_RATE_LIMIT_KEY_PREFIX + lowerCaseEmail;

        Account account = accountRepository.findByEmail(lowerCaseEmail)
                                           .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND));
        accountStatusCheck(account);

        if (account.getPassword() == null || account.getPassword().isBlank())   // OAuth 계정인 경우
            throw new BusinessException(OAUTH_USER_CANNOT_RESET_PASSWORD);

        sendPasswordResetEmailWithToken(account);

        redisRepository.setValue(rateLimitKey, "sent", Duration.ofMinutes(1));  // 1분간 재요청 금지
    }

    /**
     * 비밀번호 재설정을 확인하고, 새로운 비밀번호로 변경합니다.
     *
     * @param token   - 비밀번호 재설정 토큰
     * @param request - 새로운 비밀번호 요청 DTO
     */
    @Transactional
    @Override
    public void confirmPasswordReset(final String token, final PasswordResetConfirmRequest request) {
        if (!request.isNewPasswordConfirmed())
            throw new BusinessException(PASSWORD_MISMATCH);    // 신규 비밀번호 != 신규 비밀번호 확인

        String redisKey = REDIS_PASSWORD_RESET_KEY_PREFIX + token;
        UUID accountId = UUID.fromString(
                redisRepository.getValue(redisKey, String.class)
                               .orElseThrow(() -> new BusinessException(INVALID_VERIFICATION_TOKEN))
        );

        Account account = accountRepository.findById(accountId)
                                           .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND));
        accountStatusCheck(account);

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        redisRepository.deleteData(redisKey);
    }

    /**
     * 계정 비밀번호를 변경합니다.
     *
     * @param accountId - 계정 ID
     * @param request   - 계정 비밀번호 변경 요청 DTO
     */
    @Override
    public void changePassword(final UUID accountId, final AccountPasswordUpdateRequest request) {
        if (!request.isNewPasswordConfirmed()) throw new BusinessException(PASSWORD_MISMATCH); // 신규 비밀번호 != 신규 비밀번호 확인

        Account account = accountRepository.findById(accountId)
                                           .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND));
        accountStatusCheck(account);

        if (account.getPassword() == null
            || !passwordEncoder.matches(request.getCurrentPassword(),
                                        account.getPassword()))   // OAuth 계정 or 기존 비밀번호 불일치인 경우
            throw new BusinessException(INVALID_PASSWORD);

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    /**
     * OAuth 계정을 탈퇴 처리합니다. 계정의 탈퇴일을 현재 시간으로 설정하고, 계정 상태를 탈퇴 상태(DELETE)로 변경합니다.
     *
     * @param accountId - 계정 ID
     */
    @Transactional
    @Override
    public void withdrawAccount(final UUID accountId) {
        Account account = accountRepository.findById(accountId)
                                           .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND));
        accountStatusCheck(account);

        if (account.getOAuthConnections().isEmpty())
            throw new BusinessException(OAUTH_PROVIDER_NOT_SUPPORTED);   // OAuth 연동 정보가 없을 경우

        account.withdraw();
        account.getOAuthConnections().forEach(OAuthConnection::delete);
    }

    /**
     * 이메일 계정을 탈퇴 처리합니다. 계정의 탈퇴일을 현재 시간으로 설정하고, 계정 상태를 탈퇴 상태(DELETE)로 변경합니다.
     *
     * @param accountId - 계정 ID
     * @param request   - 계정 탈퇴 요청 DTO
     */
    @Transactional
    @Override
    public void withdrawAccount(final UUID accountId, final AccountWithdrawRequest request) {
        Account account = accountRepository.findById(accountId)
                                           .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND));
        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword()))  // 비밀번호 확인 불일치의 경우
            throw new BusinessException(INVALID_PASSWORD);

        accountStatusCheck(account);

        account.withdraw();
    }

    /**
     * 계정 엔티티를 조회합니다.
     *
     * @param accountId - 계정 ID
     * @return 조회된 계정 엔티티
     */
    @Override
    public Account findByAccountId(final UUID accountId) {
        return accountRepository.findByIdAndStatus(accountId, ACTIVE)
                                .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND));
    }

    // ========================= 내부 메서드 =========================

    /**
     * 인증 메일을 발송합니다.
     *
     * @param account - 인증 대상 계정
     */
    private void sendVerificationEmailWithToken(final Account account) {
        String verificationToken = UUID.randomUUID().toString().replace("-", "");
        redisRepository.setValue(REDIS_VERIFICATION_KEY_PREFIX + verificationToken,
                                 account.getId().toString(),
                                 Duration.ofMinutes(emailProperties.getVerificationTokenExpiryMinutes()));

        String verificationLink = emailProperties.getVerificationBaseUrl() + verificationToken;
        emailService.sendVerificationEmail(account.getEmail(), verificationLink);
    }

    /**
     * 비밀번호 초기화 메일을 발송합니다.
     *
     * @param account - 비밀번호 초기화 대상 계정
     */
    private void sendPasswordResetEmailWithToken(final Account account) {
        String passwordResetToken = UUID.randomUUID().toString().replace("-", "");
        redisRepository.setValue(REDIS_PASSWORD_RESET_KEY_PREFIX + passwordResetToken,
                                 account.getId().toString(),
                                 Duration.ofMinutes(emailProperties.getPasswordResetTokenExpiryMinutes()));

        String resetLink = emailProperties.getPasswordResetBaseUrl() + passwordResetToken;
        emailService.sendPasswordResetEmail(account.getEmail(), resetLink);
    }

    /**
     * 계정 상태를 검증합니다. 활성화(ACTIVE) 상태가 아닌 경우 예외가 발생합니다.
     *
     * @param account - 계정
     */
    private void accountStatusCheck(final Account account) {
        switch (account.getStatus()) {
            case INACTIVE:
                throw new BusinessException(ACCOUNT_INACTIVE);
            case DELETED:
                throw new BusinessException(ACCOUNT_ALREADY_WITHDRAWN);
            case BLOCKED:
                throw new BusinessException(ACCOUNT_BLOCKED);
            default:
                break;
        }
    }

}
