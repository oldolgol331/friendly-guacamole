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
import static com.example.demo.infra.redis.constant.RedisConst.PASSWORD_RESET_KEY_PREFIX;
import static com.example.demo.infra.redis.constant.RedisConst.VERIFICATION_KEY_PREFIX;
import static com.example.demo.infra.redis.constant.RedisConst.VERIFICATION_RATE_LIMIT_KEY_PREFIX;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.common.error.CustomException;
import com.example.demo.common.mail.properties.EmailProperties;
import com.example.demo.common.mail.service.EmailService;
import com.example.demo.common.util.TestUtils;
import com.example.demo.domain.account.dao.AccountRepository;
import com.example.demo.domain.account.dao.OAuthConnectionRepository;
import com.example.demo.domain.account.dto.AccountRequest.AccountPasswordUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountSignUpRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountWithdrawRequest;
import com.example.demo.domain.account.dto.AccountRequest.PasswordResetConfirmRequest;
import com.example.demo.domain.account.dto.AccountResponse.AccountInfoResponse;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.model.AccountRole;
import com.example.demo.domain.account.model.AccountStatus;
import com.example.demo.domain.account.model.OAuthConnection;
import com.example.demo.infra.redis.dao.RedisRepository;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * PackageName : com.example.demo.domain.account.service
 * FileName    : AccountServiceTest
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : AccountService 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    AccountServiceImpl        accountService;
    @Mock
    AccountRepository         accountRepository;
    @Mock
    OAuthConnectionRepository oAuthConnectionRepository;
    @Mock
    RedisRepository           redisRepository;
    @Mock
    PasswordEncoder           passwordEncoder;
    @Mock
    EmailService              emailService;
    @Mock
    EmailProperties           emailProperties;

    @Nested
    @DisplayName("signUpEmailUser() 테스트")
    class SignUpEmailUserTests {

        @RepeatedTest(10)
        @DisplayName("이메일 회원 가입")
        void signUpEmailUser() {
            // given
            AccountSignUpRequest request = TestUtils.createAccountSignUpRequest();

            String email    = request.getEmail().toLowerCase();
            String nickname = request.getNickname();
            String password = request.getPassword();

            when(accountRepository.existsByEmail(eq(email))).thenReturn(false);
            when(accountRepository.existsByNickname(eq(nickname))).thenReturn(false);
            when(accountRepository.save(any(Account.class))).thenAnswer((Answer<Account>) invocation -> {
                Account accountToSave = invocation.getArgument(0);
                ReflectionTestUtils.setField(accountToSave, "id", UUID.randomUUID());
                return accountToSave;
            });
            when(passwordEncoder.encode(eq(password))).thenReturn(password);
            doNothing().when(redisRepository).setValue(anyString(), anyString(), any(Duration.class));
            doNothing().when(emailService).sendVerificationEmail(eq(email), anyString());

            // when
            AccountInfoResponse responseData = accountService.signUpEmailUser(request);

            // then
            assertEquals(email, responseData.getEmail(), "회원 가입 요청 email과 회원 가입 결과 email은 같아야 합니다.");
            assertEquals(nickname, responseData.getNickname(), "회원 가입 요청 nickname과 회원 가입 결과 nickname은 같아야 합니다.");
            assertEquals(AccountRole.USER, responseData.getRole(), "role은 USER로 설정되어야 합니다.");
            assertEquals(AccountStatus.INACTIVE, responseData.getStatus(), "status는 INACTIVE로 설정되어야 합니다.");

            verify(accountRepository, times(1)).existsByEmail(eq(email));
            verify(accountRepository, times(1)).existsByNickname(eq(nickname));
            verify(accountRepository, times(1)).save(any(Account.class));
            verify(passwordEncoder, times(1)).encode(eq(password));
            verify(redisRepository, times(1)).setValue(anyString(), anyString(), any(Duration.class));
            verify(emailService, times(1)).sendVerificationEmail(eq(email), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 가입 시도, 비밀번호 확인 불일치")
        void signUpEmailUser_passwordConfirmFailure() {
            // given
            AccountSignUpRequest request = TestUtils.createAccountSignUpRequest();

            request.setConfirmPassword(request.getPassword() + ".");

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.signUpEmailUser(request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PASSWORD_MISMATCH, exception.getErrorCode(),
                                         "errorCode는 PASSWORD_MISMATCH여야 합니다."));

            verify(accountRepository, never()).existsByEmail(anyString());
            verify(accountRepository, never()).existsByNickname(anyString());
            verify(accountRepository, never()).save(any(Account.class));
            verify(passwordEncoder, never()).encode(anyString());
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
            verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 가입 시도, 이메일 중복")
        void signUpEmailUser_existingEmail() {
            // given
            AccountSignUpRequest request = TestUtils.createAccountSignUpRequest();

            String email = request.getEmail().toLowerCase();

            when(accountRepository.existsByEmail(eq(email))).thenReturn(true);

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.signUpEmailUser(request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(EMAIL_DUPLICATION, exception.getErrorCode(),
                                         "errorCode는 EMAIL_DUPLICATION이여야 합니다."));

            verify(accountRepository, times(1)).existsByEmail(eq(email));
            verify(accountRepository, never()).existsByNickname(anyString());
            verify(accountRepository, never()).save(any(Account.class));
            verify(passwordEncoder, never()).encode(anyString());
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
            verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 가입 시도, 닉네임 중복")
        void signUpEmailUser_existingNickname() {
            // given
            AccountSignUpRequest request = TestUtils.createAccountSignUpRequest();

            String email    = request.getEmail().toLowerCase();
            String nickname = request.getNickname();

            when(accountRepository.existsByEmail(eq(email))).thenReturn(false);
            when(accountRepository.existsByNickname(eq(nickname))).thenReturn(true);

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.signUpEmailUser(request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(NICKNAME_DUPLICATION, exception.getErrorCode(),
                                         "errorCode는 NICKNAME_DUPLICATION이여야 합니다."));

            verify(accountRepository, times(1)).existsByEmail(eq(email));
            verify(accountRepository, times(1)).existsByNickname(eq(nickname));
            verify(accountRepository, never()).save(any(Account.class));
            verify(passwordEncoder, never()).encode(anyString());
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
            verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
        }

    }

    @Nested
    @DisplayName("findOrCreateAccountForOAuth() 테스트")
    class FindOrCreateAccountForOAuthTests {

        @RepeatedTest(10)
        @DisplayName("OAuth 신규 회원 가입")
        void findOrCreateAccountForOAuth_createNewAccount() {
            // given
            String provider          = TestUtils.FAKER.company().name();
            String providerId        = UUID.randomUUID().toString();
            String emailFromOAuth    = TestUtils.FAKER.internet().emailAddress();
            String nicknameFromOAuth = TestUtils.FAKER.credentials().username().replace(".", "").substring(0, 5);

            Account account = Account.of(emailFromOAuth, nicknameFromOAuth, provider, providerId);
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());
            OAuthConnection oAuthConnection = OAuthConnection.of(account, provider, providerId);

            when(accountRepository.findByProviderAndProviderId(eq(provider), eq(providerId)))
                    .thenReturn(Optional.empty());
            when(accountRepository.findByEmail(eq(emailFromOAuth))).thenReturn(Optional.empty());
            when(accountRepository.existsByNickname(eq(nicknameFromOAuth))).thenReturn(false);
            when(accountRepository.save(any(Account.class))).thenReturn(account);
            when(oAuthConnectionRepository.save(any(OAuthConnection.class))).thenReturn(oAuthConnection);

            // when
            Account savedAccount = accountService.findOrCreateAccountForOAuth(provider,
                                                                              providerId,
                                                                              emailFromOAuth,
                                                                              nicknameFromOAuth);

            // then
            assertEquals(emailFromOAuth, savedAccount.getEmail(), "OAuth 회원 가입 요청 email과 회원 가입 결과 email은 같아야 합니다.");
            assertEquals(nicknameFromOAuth, savedAccount.getNickname(),
                         "OAuth 회원 가입 요청 nickname과 회원 가입 결과 nickname은 같아야 합니다.");
            assertEquals(provider, savedAccount.getOAuthConnections().getFirst().getProvider(),
                         "OAuth 회원 가입 요청 provider와 회원 가입 결과의 OAuth 연동 정보 provider는 같아야 합니다.");
            assertEquals(providerId, savedAccount.getOAuthConnections().getFirst().getProviderId(),
                         "OAuth 회원 가입 요청 providerId와 회원 가입 결과의 OAuth 연동 정보 providerId는 같아야 합니다.");

            verify(accountRepository, times(1)).findByProviderAndProviderId(eq(provider), eq(providerId));
            verify(accountRepository, times(1)).findByEmail(eq(emailFromOAuth));
            verify(accountRepository, times(1)).existsByNickname(eq(nicknameFromOAuth));
            verify(accountRepository, times(1)).save(any(Account.class));
            verify(oAuthConnectionRepository, times(1)).save(any(OAuthConnection.class));
        }

        @RepeatedTest(10)
        @DisplayName("기존 회원에 OAuth 연동 추가")
        void findOrCreateAccountForOAuth_connectExistingAccount() {
            // given
            String provider          = TestUtils.FAKER.company().name();
            String providerId        = UUID.randomUUID().toString();
            String emailFromOAuth    = TestUtils.FAKER.internet().emailAddress();
            String nicknameFromOAuth = TestUtils.FAKER.credentials().username().replace(".", "").substring(0, 5);

            Account account = Account.of(emailFromOAuth, nicknameFromOAuth, provider, providerId);
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());
            OAuthConnection oAuthConnection = OAuthConnection.of(account, provider, providerId);

            when(accountRepository.findByProviderAndProviderId(eq(provider), eq(providerId)))
                    .thenReturn(Optional.empty());
            when(accountRepository.findByEmail(eq(emailFromOAuth))).thenReturn(Optional.of(account));
            when(oAuthConnectionRepository.save(any(OAuthConnection.class))).thenReturn(oAuthConnection);

            // when
            Account accountToLink = accountService.findOrCreateAccountForOAuth(provider,
                                                                               providerId,
                                                                               emailFromOAuth,
                                                                               nicknameFromOAuth);

            // then
            assertEquals(emailFromOAuth, accountToLink.getEmail(), "OAuth 회원 가입 요청 email과 회원 가입 결과 email은 같아야 합니다.");
            assertEquals(nicknameFromOAuth, accountToLink.getNickname(),
                         "OAuth 회원 가입 요청 nickname과 회원 가입 결과 nickname은 같아야 합니다.");
            assertEquals(provider, accountToLink.getOAuthConnections().getFirst().getProvider(),
                         "OAuth 회원 가입 요청 provider와 회원 가입 결과의 OAuth 연동 정보 provider는 같아야 합니다.");
            assertEquals(providerId, accountToLink.getOAuthConnections().getFirst().getProviderId(),
                         "OAuth 회원 가입 요청 providerId와 회원 가입 결과의 OAuth 연동 정보 providerId는 같아야 합니다.");

            verify(accountRepository, times(1)).findByProviderAndProviderId(eq(provider), eq(providerId));
            verify(accountRepository, times(1)).findByEmail(eq(emailFromOAuth));
            verify(oAuthConnectionRepository, times(1)).save(any(OAuthConnection.class));
            verify(accountRepository, never()).existsByNickname(eq(nicknameFromOAuth));
            verify(accountRepository, never()).save(any(Account.class));
        }

    }

    @Nested
    @DisplayName("resendVerificationEmail() 테스트")
    class ResendVerificationEmailTests {

        @RepeatedTest(10)
        @DisplayName("이메일 회원 인증 메일 재요청")
        void resendVerificationEmail() {
            // given
            String email = TestUtils.FAKER.internet().emailAddress().toLowerCase();

            Account account = Account.of(email,
                                         TestUtils.createPassword(),
                                         TestUtils.FAKER.credentials().username().replace(".", "").substring(0, 5));
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());

            when(redisRepository.hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email))).thenReturn(false);
            when(accountRepository.findByEmail(eq(email))).thenReturn(Optional.of(account));
            doNothing().when(emailService).sendVerificationEmail(eq(email), anyString());
            doNothing().when(redisRepository).setValue(anyString(), anyString(), any(Duration.class));

            // when
            accountService.resendVerificationEmail(email);

            // then
            verify(redisRepository, times(1)).hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email));
            verify(accountRepository, times(1)).findByEmail(eq(email));
            verify(emailService, times(1)).sendVerificationEmail(eq(email), anyString());
            verify(redisRepository, times(2)).setValue(anyString(), anyString(), any(Duration.class));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 인증 메일 재요청 시도, 요청 횟수 제한")
        void resendVerificationEmail_limitCountOfRequests() {
            // given
            String email = TestUtils.FAKER.internet().emailAddress().toLowerCase();

            when(redisRepository.hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email))).thenReturn(true);

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.resendVerificationEmail(email),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(TOO_MANY_REQUESTS, exception.getErrorCode(),
                                         "errorCode는 TOO_MANY_REQUESTS여야 합니다."));

            verify(redisRepository, times(1)).hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email));
            verify(accountRepository, never()).findByEmail(eq(email));
            verify(emailService, never()).sendVerificationEmail(eq(email), anyString());
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 인증 메일 재요청 시도, 해당 이메일 회원이 없음")
        void resendVerificationEmail_notFoundAccount() {
            // given
            String email = TestUtils.FAKER.internet().emailAddress().toLowerCase();

            when(redisRepository.hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email))).thenReturn(false);
            when(accountRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.resendVerificationEmail(email),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_NOT_FOUND여야 합니다."));

            verify(redisRepository, times(1)).hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email));
            verify(accountRepository, times(1)).findByEmail(eq(email));
            verify(emailService, never()).sendVerificationEmail(eq(email), anyString());
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 인증 메일 재요청 시도, 해당 이메일 회원이 이미 인증된 상태")
        void resendVerificationEmail_invalidStatus_alreadyVerifiedEmail() {
            // given
            String email = TestUtils.FAKER.internet().emailAddress().toLowerCase();

            Account account = Account.of(email,
                                         TestUtils.createPassword(),
                                         TestUtils.FAKER.credentials().username().replace(".", "").substring(0, 5));
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());

            account.setStatus(AccountStatus.ACTIVE);

            when(redisRepository.hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email))).thenReturn(false);
            when(accountRepository.findByEmail(eq(email))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.resendVerificationEmail(email),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ALREADY_VERIFIED_EMAIL, exception.getErrorCode(),
                                         "errorCode는 ALREADY_VERIFIED_EMAIL이여야 합니다."));

            verify(redisRepository, times(1)).hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email));
            verify(accountRepository, times(1)).findByEmail(eq(email));
            verify(emailService, never()).sendVerificationEmail(eq(email), anyString());
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 인증 메일 재요청 시도, 해당 이메일 회원이 탈퇴한 상태")
        void resendVerificationEmail_invalidStatus_alreadyWithdrawn() {
            // given
            String email = TestUtils.FAKER.internet().emailAddress().toLowerCase();

            Account account = Account.of(email,
                                         TestUtils.createPassword(),
                                         TestUtils.FAKER.credentials().username().replace(".", "").substring(0, 5));
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());

            account.setStatus(AccountStatus.DELETED);

            when(redisRepository.hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email))).thenReturn(false);
            when(accountRepository.findByEmail(eq(email))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.resendVerificationEmail(email),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_ALREADY_WITHDRAWN, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_ALREADY_WITHDRAWN이여야 합니다."));

            verify(redisRepository, times(1)).hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email));
            verify(accountRepository, times(1)).findByEmail(eq(email));
            verify(emailService, never()).sendVerificationEmail(eq(email), anyString());
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 인증 메일 재요청 시도, 해당 이메일 회원이 차단된 상태")
        void resendVerificationEmail_invalidStatus_blocked() {
            // given
            String email = TestUtils.FAKER.internet().emailAddress().toLowerCase();

            Account account = Account.of(email,
                                         TestUtils.createPassword(),
                                         TestUtils.FAKER.credentials().username().replace(".", "").substring(0, 5));
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());

            account.setStatus(AccountStatus.BLOCKED);

            when(redisRepository.hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email))).thenReturn(false);
            when(accountRepository.findByEmail(eq(email))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.resendVerificationEmail(email),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_BLOCKED, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_BLOCKED여야 합니다."));

            verify(redisRepository, times(1)).hasKey(eq(VERIFICATION_RATE_LIMIT_KEY_PREFIX + email));
            verify(accountRepository, times(1)).findByEmail(eq(email));
            verify(emailService, never()).sendVerificationEmail(eq(email), anyString());
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
        }

    }

    @Nested
    @DisplayName("verifyEmail() 테스트")
    class VerifyEmailTests {

        @RepeatedTest(10)
        @DisplayName("이메일 인증")
        void verifyEmail() {
            // given
            String token = UUID.randomUUID().toString();

            Account account = Account.of(TestUtils.FAKER.internet().emailAddress().toLowerCase(),
                                         TestUtils.createPassword(),
                                         TestUtils.FAKER.credentials().username().replace(".", "").substring(0, 5));
            UUID id = UUID.randomUUID();
            ReflectionTestUtils.setField(account, "id", id);

            String redisKey = VERIFICATION_KEY_PREFIX + token;

            when(redisRepository.getValue(eq(redisKey), eq(String.class))).thenReturn(Optional.of(id.toString()));
            when(accountRepository.findById(eq(id))).thenReturn(Optional.of(account));
            when(redisRepository.deleteData(eq(redisKey))).thenReturn(true);

            // when
            AccountInfoResponse responseData = accountService.verifyEmail(token);

            // then
            assertEquals(AccountStatus.ACTIVE, responseData.getStatus(), "status는 ACTIVE로 설정되어야 합니다.");

            verify(redisRepository, times(1)).getValue(eq(redisKey), eq(String.class));
            verify(accountRepository, times(1)).findById(eq(id));
            verify(redisRepository, times(1)).deleteData(eq(redisKey));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 인증 시도, 무효한 인증 토큰")
        void verifyEmail_invalidToken() {
            // given
            String token = UUID.randomUUID().toString();

            String redisKey = VERIFICATION_KEY_PREFIX + token;

            when(redisRepository.getValue(eq(redisKey), eq(String.class))).thenReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class, () -> accountService.verifyEmail(token),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(INVALID_VERIFICATION_TOKEN, exception.getErrorCode(),
                                         "errorCode는 INVALID_VERIFICATION_TOKEN이여야 합니다."));

            verify(redisRepository, times(1)).getValue(eq(redisKey), eq(String.class));
            verify(accountRepository, never()).findById(any(UUID.class));
            verify(redisRepository, never()).deleteData(eq(redisKey));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 인증 시도, 해당 이메일 회원이 없음")
        void verifyEmail_notFoundAccount() {
            // given
            String token = UUID.randomUUID().toString();
            UUID   id    = UUID.randomUUID();

            String redisKey = VERIFICATION_KEY_PREFIX + token;

            when(redisRepository.getValue(eq(redisKey), eq(String.class))).thenReturn(Optional.of(id.toString()));
            when(accountRepository.findById(eq(id))).thenReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class, () -> accountService.verifyEmail(token),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_NOT_FOUND여야 합니다."));

            verify(redisRepository, times(1)).getValue(eq(redisKey), eq(String.class));
            verify(accountRepository, times(1)).findById(eq(id));
            verify(redisRepository, never()).deleteData(eq(redisKey));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 인증 시도, 이미 인증된 회원")
        void verifyEmail_invalidStatus_alreadyVerifiedEmail() {
            // given
            String token = UUID.randomUUID().toString();

            Account account = Account.of(TestUtils.FAKER.internet().emailAddress().toLowerCase(),
                                         TestUtils.createPassword(),
                                         TestUtils.FAKER.credentials().username().replace(".", "").substring(0, 5));
            UUID id = UUID.randomUUID();
            ReflectionTestUtils.setField(account, "id", id);

            account.setStatus(AccountStatus.ACTIVE);

            String redisKey = VERIFICATION_KEY_PREFIX + token;

            when(redisRepository.getValue(eq(redisKey), eq(String.class))).thenReturn(Optional.of(id.toString()));
            when(accountRepository.findById(eq(id))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class, () -> accountService.verifyEmail(token),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ALREADY_VERIFIED_EMAIL, exception.getErrorCode(),
                                         "errorCode는 ALREADY_VERIFIED_EMAIL이여야 합니다."));

            verify(redisRepository, times(1)).getValue(eq(redisKey), eq(String.class));
            verify(accountRepository, times(1)).findById(eq(id));
            verify(redisRepository, never()).deleteData(eq(redisKey));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 인증 시도, 이미 탈퇴한 회원")
        void verifyEmail_invalidStatus_alreadyWithdrawn() {
            // given
            String token = UUID.randomUUID().toString();

            Account account = Account.of(TestUtils.FAKER.internet().emailAddress().toLowerCase(),
                                         TestUtils.createPassword(),
                                         TestUtils.FAKER.credentials().username().replace(".", "").substring(0, 5));
            UUID id = UUID.randomUUID();
            ReflectionTestUtils.setField(account, "id", id);

            account.setStatus(AccountStatus.DELETED);

            String redisKey = VERIFICATION_KEY_PREFIX + token;

            when(redisRepository.getValue(eq(redisKey), eq(String.class))).thenReturn(Optional.of(id.toString()));
            when(accountRepository.findById(eq(id))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class, () -> accountService.verifyEmail(token),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_ALREADY_WITHDRAWN, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_ALREADY_WITHDRAWN이여야 합니다."));

            verify(redisRepository, times(1)).getValue(eq(redisKey), eq(String.class));
            verify(accountRepository, times(1)).findById(eq(id));
            verify(redisRepository, never()).deleteData(eq(redisKey));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 인증 시도, 차단된 회원")
        void verifyEmail_invalidStatus_blocked() {
            // given
            String token = UUID.randomUUID().toString();

            Account account = Account.of(TestUtils.FAKER.internet().emailAddress().toLowerCase(),
                                         TestUtils.createPassword(),
                                         TestUtils.FAKER.credentials().username().replace(".", "").substring(0, 5));
            UUID id = UUID.randomUUID();
            ReflectionTestUtils.setField(account, "id", id);

            account.setStatus(AccountStatus.BLOCKED);

            String redisKey = VERIFICATION_KEY_PREFIX + token;

            when(redisRepository.getValue(eq(redisKey), eq(String.class))).thenReturn(Optional.of(id.toString()));
            when(accountRepository.findById(eq(id))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class, () -> accountService.verifyEmail(token),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_BLOCKED, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_BLOCKED여야 합니다."));

            verify(redisRepository, times(1)).getValue(eq(redisKey), eq(String.class));
            verify(accountRepository, times(1)).findById(eq(id));
            verify(redisRepository, never()).deleteData(eq(redisKey));
        }

    }

    @Nested
    @DisplayName("getAccountInfoById() 테스트")
    class GetAccountInfoByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 회원 정보 조회")
        void getAccountInfoById() {
            // given
            UUID accountId = UUID.randomUUID();

            AccountInfoResponse accountInfoResponse = TestUtils.createAccountInfoResponse();

            when(accountRepository.getAccountInfoResponseByIdAndStatus(eq(accountId), eq(AccountStatus.ACTIVE)))
                    .thenReturn(Optional.of(accountInfoResponse));

            // when
            AccountInfoResponse responseData = accountService.getAccountInfoById(accountId);

            // then
            assertNotNull(responseData, "responseData는 null이 아니어야 합니다.");
            assertEquals(accountInfoResponse.getId(), responseData.getId(), "id는 같아야 합니다.");
            assertEquals(accountInfoResponse.getEmail(), responseData.getEmail(), "email은 같아야 합니다.");
            assertEquals(accountInfoResponse.getNickname(), responseData.getNickname(), "nickname은 같아야 합니다.");
            assertEquals(accountInfoResponse.getRole(), responseData.getRole(), "role은 같아야 합니다.");
            assertEquals(accountInfoResponse.getStatus(), responseData.getStatus(), "status는 같아야 합니다.");
            assertEquals(accountInfoResponse.getProviders(), responseData.getProviders(), "providers는 같아야 합니다.");
            assertEquals(accountInfoResponse.getCreatedAt(), responseData.getCreatedAt(), "createdAt은 같아야 합니다.");
            assertEquals(accountInfoResponse.getUpdatedAt(), responseData.getUpdatedAt(), "updatedAt은 같아야 합니다.");

            verify(accountRepository, times(1)).getAccountInfoResponseByIdAndStatus(eq(accountId),
                                                                                    eq(AccountStatus.ACTIVE));
        }

        @RepeatedTest(10)
        @DisplayName("id로 회원 정보 조회 시도, 해당 회원이 없음")
        void getAccountInfoById_notFoundAccount() {
            // given
            UUID accountId = UUID.randomUUID();

            when(accountRepository.getAccountInfoResponseByIdAndStatus(eq(accountId), eq(AccountStatus.ACTIVE)))
                    .thenReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.getAccountInfoById(accountId),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_NOT_FOUND여야 합니다."));

            verify(accountRepository, times(1)).getAccountInfoResponseByIdAndStatus(eq(accountId),
                                                                                    eq(AccountStatus.ACTIVE));
        }

    }

    @Nested
    @DisplayName("updateAccountInfo() 테스트")
    class UpdateAccountInfoTests {

        @RepeatedTest(10)
        @DisplayName("회원 정보 업데이트")
        void updateAccountInfo() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.ACTIVE);
            String currentPassword = account.getPassword();

            AccountUpdateRequest request = TestUtils.createAccountUpdateRequest(currentPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(eq(currentPassword), eq(account.getPassword()))).thenReturn(true);
            when(accountRepository.existsByNickname(eq(request.getNewNickname()))).thenReturn(false);

            // when
            AccountInfoResponse responseData = accountService.updateAccountInfo(accountId, request);

            // then
            assertNotNull(responseData, "responseData는 null이 아니어야 합니다.");
            assertEquals(request.getNewNickname(), responseData.getNickname(), "nickname은 newNickname 값과 같아야 합니다.");

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, times(1)).matches(eq(currentPassword), eq(account.getPassword()));
            verify(accountRepository, times(1)).existsByNickname(eq(request.getNewNickname()));
        }

        @RepeatedTest(10)
        @DisplayName("회원 정보 업데이트 시도, 해당 회원이 없음")
        void updateAccountInfo_notFoundAccount() {
            // given
            UUID accountId = UUID.randomUUID();

            AccountUpdateRequest request = TestUtils.createAccountUpdateRequest(TestUtils.createPassword());

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.updateAccountInfo(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_NOT_FOUND여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(accountRepository, never()).existsByNickname(eq(request.getNewNickname()));
        }

        @RepeatedTest(10)
        @DisplayName("회원 정보 업데이트 시도, 비밀번호 확인 불일치")
        void updateAccountInfo_passwordMismatch() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.ACTIVE);
            String wrongPassword = account.getPassword() + ".";

            AccountUpdateRequest request = TestUtils.createAccountUpdateRequest(wrongPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(eq(wrongPassword), eq(account.getPassword()))).thenReturn(false);

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.updateAccountInfo(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(INVALID_PASSWORD, exception.getErrorCode(),
                                         "errorCode는 PASSWORD_MISMATCH여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, times(1)).matches(eq(wrongPassword), eq(account.getPassword()));
            verify(accountRepository, never()).existsByNickname(eq(request.getNewNickname()));
        }

        @RepeatedTest(10)
        @DisplayName("회원 정보 업데이트 시도, 이미 사용 중인 닉네임")
        void updateAccountInfo_nicknameDuplicate() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.ACTIVE);
            String currentPassword = account.getPassword();

            AccountUpdateRequest request = TestUtils.createAccountUpdateRequest(currentPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(eq(currentPassword), eq(account.getPassword()))).thenReturn(true);
            when(accountRepository.existsByNickname(eq(request.getNewNickname()))).thenReturn(true);

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.updateAccountInfo(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(NICKNAME_DUPLICATION, exception.getErrorCode(),
                                         "errorCode는 NICKNAME_DUPLICATION이여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, times(1)).matches(eq(currentPassword), eq(account.getPassword()));
            verify(accountRepository, times(1)).existsByNickname(eq(request.getNewNickname()));
        }

    }

    @Nested
    @DisplayName("sendPasswordResetEmail() 테스트")
    class SendPasswordResetEmailTests {

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 이메일 발송")
        void sendPasswordResetEmail() {
            // given
            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());
            account.setStatus(AccountStatus.ACTIVE);
            String lowerCaseEmail = account.getEmail().toLowerCase();

            when(accountRepository.findByEmail(eq(lowerCaseEmail))).thenReturn(Optional.of(account));
            doNothing().when(redisRepository).setValue(anyString(), anyString(), any(Duration.class));
            doNothing().when(emailService).sendPasswordResetEmail(eq(lowerCaseEmail), anyString());

            // when
            accountService.sendPasswordResetEmail(lowerCaseEmail);

            // then
            verify(accountRepository, times(1)).findByEmail(eq(lowerCaseEmail));
            verify(redisRepository, times(2)).setValue(anyString(), anyString(), any(Duration.class));
            verify(emailService, times(1)).sendPasswordResetEmail(eq(lowerCaseEmail), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 이메일 발송 시도, 해당 회원이 없음")
        void sendPasswordResetEmail_notFoundAccount() {
            // given
            String email = TestUtils.FAKER.internet().safeEmailAddress();

            when(accountRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.sendPasswordResetEmail(email),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_NOT_FOUND여야 합니다."));

            verify(accountRepository, times(1)).findByEmail(eq(email));
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
            verify(emailService, never()).sendPasswordResetEmail(eq(email), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 이메일 발송 시도, 인증되지 않은 회원")
        void sendPasswordResetEmail_invalidStatus_inactive() {
            // given
            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());
            String email = account.getEmail();

            when(accountRepository.findByEmail(eq(email))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.sendPasswordResetEmail(email),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_INACTIVE, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_INACTIVE여야 합니다."));

            verify(accountRepository, times(1)).findByEmail(eq(email));
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
            verify(emailService, never()).sendPasswordResetEmail(eq(email), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 이메일 발송 시도, 이미 탈퇴한 회원")
        void sendPasswordResetEmail_invalidStatus_alreadyWithdrawn() {
            // given
            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());
            account.setStatus(AccountStatus.DELETED);
            String email = account.getEmail();

            when(accountRepository.findByEmail(eq(email))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.sendPasswordResetEmail(email),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_ALREADY_WITHDRAWN, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_ALREADY_WITHDRAWN이여야 합니다."));

            verify(accountRepository, times(1)).findByEmail(eq(email));
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
            verify(emailService, never()).sendPasswordResetEmail(eq(email), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 이메일 발송 시도, 차단된 회원")
        void sendPasswordResetEmail_invalidStatus_blocked() {
            // given
            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());
            account.setStatus(AccountStatus.BLOCKED);
            String email = account.getEmail();

            when(accountRepository.findByEmail(eq(email))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.sendPasswordResetEmail(email),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_BLOCKED, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_BLOCKED여야 합니다."));

            verify(accountRepository, times(1)).findByEmail(eq(email));
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
            verify(emailService, never()).sendPasswordResetEmail(eq(email), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 이메일 발송 시도, OAuth 연동 회원")
        void sendPasswordResetEmail_oAuthConnectionOnly() {
            // given
            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(account, "password", null);
            account.setStatus(AccountStatus.ACTIVE);
            String email = account.getEmail();

            when(accountRepository.findByEmail(eq(email))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.sendPasswordResetEmail(email),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(OAUTH_USER_CANNOT_RESET_PASSWORD, exception.getErrorCode(),
                                         "errorCode는 OAUTH_USER_CANNOT_RESET_PASSWORD이여야 합니다."));

            verify(accountRepository, times(1)).findByEmail(eq(email));
            verify(redisRepository, never()).setValue(anyString(), anyString(), any(Duration.class));
            verify(emailService, never()).sendPasswordResetEmail(eq(email), anyString());
        }

    }

    @Nested
    @DisplayName("confirmPasswordReset() 테스트")
    class ConfirmPasswordResetTests {

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 확인 및 신규 비밀번호 변경")
        void confirmPasswordReset() {
            // given
            Account account   = TestUtils.createAccount();
            UUID    accountId = UUID.randomUUID();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.ACTIVE);

            String                      newPassword = TestUtils.createPassword();
            PasswordResetConfirmRequest request     = new PasswordResetConfirmRequest(newPassword, newPassword);

            String token    = UUID.randomUUID().toString().replace("-", "");
            String redisKey = PASSWORD_RESET_KEY_PREFIX + token;

            when(redisRepository.getValue(eq(redisKey), eq(String.class))).thenReturn(
                    Optional.of(accountId.toString()));
            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));
            when(passwordEncoder.encode(eq(newPassword))).thenReturn(newPassword);
            when(redisRepository.deleteData(eq(redisKey))).thenReturn(true);

            // when
            accountService.confirmPasswordReset(token, request);

            // then
            verify(redisRepository, times(1)).getValue(eq(redisKey), eq(String.class));
            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, times(1)).encode(eq(newPassword));
            verify(redisRepository, times(1)).deleteData(eq(redisKey));
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 확인 및 신규 비밀번호 변경 시도, 비밀번호 확인 불일치")
        void confirmPasswordReset_passwordMismatch() {
            // given
            String                      newPassword = TestUtils.createPassword();
            PasswordResetConfirmRequest request     = new PasswordResetConfirmRequest(newPassword, newPassword + "!");

            String token = UUID.randomUUID().toString().replace("-", "");

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.confirmPasswordReset(token, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다. "),
                      () -> assertEquals(PASSWORD_MISMATCH,
                                         exception.getErrorCode(),
                                         "errorCode는 PASSWORD_MISMATCH여야 합니다."));

            verify(redisRepository, never()).getValue(anyString(), eq(String.class));
            verify(accountRepository, never()).findById(any(UUID.class));
            verify(passwordEncoder, never()).encode(eq(newPassword));
            verify(redisRepository, never()).deleteData(anyString());
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 확인 및 신규 비밀번호 변경 시도, 무효한 인증 토큰")
        void confirmPasswordReset_invalidToken() {
            // given
            String                      newPassword = TestUtils.createPassword();
            PasswordResetConfirmRequest request     = new PasswordResetConfirmRequest(newPassword, newPassword);

            String token = UUID.randomUUID().toString().replace("-", "");

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.confirmPasswordReset(token, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다. "),
                      () -> assertEquals(INVALID_VERIFICATION_TOKEN,
                                         exception.getErrorCode(),
                                         "errorCode는 INVALID_VERIFICATION_TOKEN여야 합니다."));

            verify(redisRepository, times(1)).getValue(anyString(), eq(String.class));
            verify(accountRepository, never()).findById(any(UUID.class));
            verify(passwordEncoder, never()).encode(eq(newPassword));
            verify(redisRepository, never()).deleteData(anyString());
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 확인 및 신규 비밀번호 변경 시도, 해당 회원이 없음")
        void confirmPasswordReset_notFoundAccount() {
            // given
            UUID accountId = UUID.randomUUID();

            String                      newPassword = TestUtils.createPassword();
            PasswordResetConfirmRequest request     = new PasswordResetConfirmRequest(newPassword, newPassword);

            String token    = UUID.randomUUID().toString().replace("-", "");
            String redisKey = PASSWORD_RESET_KEY_PREFIX + token;

            when(redisRepository.getValue(eq(redisKey), eq(String.class))).thenReturn(
                    Optional.of(accountId.toString()));
            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.confirmPasswordReset(token, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다. "),
                      () -> assertEquals(ACCOUNT_NOT_FOUND,
                                         exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_NOT_FOUND여야 합니다."));

            verify(redisRepository, times(1)).getValue(eq(redisKey), eq(String.class));
            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, never()).encode(eq(newPassword));
            verify(redisRepository, never()).deleteData(eq(redisKey));
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 확인 및 신규 비밀번호 변경 시도, 인증되지 않은 회원")
        void confirmPasswordReset_invalidStatus_inactive() {
            // given
            Account account   = TestUtils.createAccount();
            UUID    accountId = UUID.randomUUID();
            ReflectionTestUtils.setField(account, "id", accountId);

            String                      newPassword = TestUtils.createPassword();
            PasswordResetConfirmRequest request     = new PasswordResetConfirmRequest(newPassword, newPassword);

            String token    = UUID.randomUUID().toString().replace("-", "");
            String redisKey = PASSWORD_RESET_KEY_PREFIX + token;

            when(redisRepository.getValue(eq(redisKey), eq(String.class))).thenReturn(
                    Optional.of(accountId.toString()));
            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.confirmPasswordReset(token, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_INACTIVE,
                                         exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_INACTIVE여야 합니다."));

            verify(redisRepository, times(1)).getValue(eq(redisKey), eq(String.class));
            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, never()).encode(eq(newPassword));
            verify(redisRepository, never()).deleteData(eq(redisKey));
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 확인 및 신규 비밀번호 변경 시도, 이미 탈퇴한 회원")
        void confirmPasswordReset_invalidStatus_alreadyWithdrawn() {
            // given
            Account account   = TestUtils.createAccount();
            UUID    accountId = UUID.randomUUID();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.DELETED);

            String                      newPassword = TestUtils.createPassword();
            PasswordResetConfirmRequest request     = new PasswordResetConfirmRequest(newPassword, newPassword);

            String token    = UUID.randomUUID().toString().replace("-", "");
            String redisKey = PASSWORD_RESET_KEY_PREFIX + token;

            when(redisRepository.getValue(eq(redisKey), eq(String.class))).thenReturn(
                    Optional.of(accountId.toString()));
            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.confirmPasswordReset(token, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다. "),
                      () -> assertEquals(ACCOUNT_ALREADY_WITHDRAWN,
                                         exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_ALREADY_WITHDRAWN이여야 합니다."));

            verify(redisRepository, times(1)).getValue(eq(redisKey), eq(String.class));
            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, never()).encode(eq(newPassword));
            verify(redisRepository, never()).deleteData(eq(redisKey));
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 재설정 확인 및 신규 비밀번호 변경 시도, 차단된 회원")
        void confirmPasswordReset_invalidStatus_blocked() {
            // given
            Account account   = TestUtils.createAccount();
            UUID    accountId = UUID.randomUUID();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.BLOCKED);

            String                      newPassword = TestUtils.createPassword();
            PasswordResetConfirmRequest request     = new PasswordResetConfirmRequest(newPassword, newPassword);

            String token    = UUID.randomUUID().toString().replace("-", "");
            String redisKey = PASSWORD_RESET_KEY_PREFIX + token;

            when(redisRepository.getValue(eq(redisKey), eq(String.class))).thenReturn(
                    Optional.of(accountId.toString()));
            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.confirmPasswordReset(token, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다. "),
                      () -> assertEquals(ACCOUNT_BLOCKED,
                                         exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_BLOCKED여야 합니다."));

            verify(redisRepository, times(1)).getValue(eq(redisKey), eq(String.class));
            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, never()).encode(eq(newPassword));
            verify(redisRepository, never()).deleteData(eq(redisKey));
        }

    }

    @Nested
    @DisplayName("changePassword() 테스트")
    class ChangePasswordTests {

        @RepeatedTest(10)
        @DisplayName("비밀번호 변경")
        void changePassword() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.ACTIVE);
            String currentPassword = account.getPassword();

            AccountPasswordUpdateRequest request = TestUtils.createAccountPasswordUpdateRequest(currentPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(eq(request.getCurrentPassword()), eq(currentPassword))).thenReturn(true);
            when(passwordEncoder.encode(eq(request.getNewPassword()))).thenReturn(request.getNewPassword());

            // when
            accountService.changePassword(accountId, request);

            // then
            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, times(1)).matches(eq(request.getCurrentPassword()), eq(currentPassword));
            verify(passwordEncoder, times(1)).encode(eq(request.getNewPassword()));
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 변경 시도, 비밀번호 확인 불일치")
        void changePassword_passwordMismatch() {
            // given
            UUID accountId = UUID.randomUUID();

            String currentPassword = TestUtils.createPassword();

            AccountPasswordUpdateRequest request = TestUtils.createAccountPasswordUpdateRequest(currentPassword);
            request.setConfirmNewPassword(request.getNewPassword() + ".");

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.changePassword(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception이 null이 아니어야 합니다."),
                      () -> assertEquals(PASSWORD_MISMATCH, exception.getErrorCode(),
                                         "errorCode는 PASSWORD_MISMATCH여야 합니다."));

            verify(accountRepository, never()).findById(eq(accountId));
            verify(passwordEncoder, never()).matches(eq(request.getCurrentPassword()), eq(currentPassword));
            verify(passwordEncoder, never()).encode(eq(request.getNewPassword()));
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 변경 시도, 해당 회원이 없음")
        void changePassword_notFoundAccount() {
            // given
            UUID accountId = UUID.randomUUID();

            AccountPasswordUpdateRequest request = TestUtils.createAccountPasswordUpdateRequest(
                    TestUtils.createPassword()
            );

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.changePassword(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_NOT_FOUND여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, never()).matches(eq(request.getCurrentPassword()), anyString());
            verify(passwordEncoder, never()).encode(eq(request.getNewPassword()));
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 변경 시도, 인증되지 않은 회원")
        void changePassword_invalidStatus_inactive() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            String currentPassword = account.getPassword();

            AccountPasswordUpdateRequest request = TestUtils.createAccountPasswordUpdateRequest(currentPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.changePassword(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_INACTIVE, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_INACTIVE여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, never()).matches(eq(request.getCurrentPassword()), eq(currentPassword));
            verify(passwordEncoder, never()).encode(eq(request.getNewPassword()));
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 변경 시도, 이미 탈퇴한 회원")
        void changePassword_invalidStatus_alreadyWithdrawn() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            String currentPassword = account.getPassword();
            account.setStatus(AccountStatus.DELETED);

            AccountPasswordUpdateRequest request = TestUtils.createAccountPasswordUpdateRequest(currentPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.changePassword(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_ALREADY_WITHDRAWN, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_ALREADY_WITHDRAWN이여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, never()).matches(eq(request.getCurrentPassword()), eq(currentPassword));
            verify(passwordEncoder, never()).encode(eq(request.getNewPassword()));
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 변경 시도, 차단된 회원")
        void changePassword_invalidStatus_blocked() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            String currentPassword = account.getPassword();
            account.setStatus(AccountStatus.BLOCKED);

            AccountPasswordUpdateRequest request = TestUtils.createAccountPasswordUpdateRequest(currentPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.changePassword(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_BLOCKED, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_BLOCKED여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, never()).matches(eq(request.getCurrentPassword()), eq(currentPassword));
            verify(passwordEncoder, never()).encode(eq(request.getNewPassword()));
        }

        @RepeatedTest(10)
        @DisplayName("비밀번호 변경 시도, 유효하지 않은 비밀번호")
        void changePassword_invalidPassword() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.ACTIVE);
            String currentPassword = account.getPassword();

            AccountPasswordUpdateRequest request = TestUtils.createAccountPasswordUpdateRequest(currentPassword + ".");

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(eq(request.getCurrentPassword()), eq(currentPassword))).thenReturn(false);

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.changePassword(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(INVALID_PASSWORD, exception.getErrorCode(),
                                         "errorCode는 INVALID_PASSWORD여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, times(1)).matches(eq(request.getCurrentPassword()), eq(currentPassword));
            verify(passwordEncoder, never()).encode(eq(request.getNewPassword()));
        }

    }

    @Nested
    @DisplayName("withdrawAccount() 테스트")
    class WithdrawAccountTests {

        @RepeatedTest(10)
        @DisplayName("OAuth 회원 탈퇴")
        void withdrawAccount() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.ACTIVE);
            TestUtils.createOAuthConnection(account);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));

            // when
            accountService.withdrawAccount(accountId);

            // then
            assertEquals(AccountStatus.DELETED, account.getStatus(), "status는 DELETED로 설정되어야 합니다.");

            verify(accountRepository, times(1)).findById(eq(accountId));
        }

        @RepeatedTest(10)
        @DisplayName("OAuth 회원 탈퇴 시도, 해당 회원이 없음")
        void withdrawAccount_notFoundAccount() {
            // given
            UUID accountId = UUID.randomUUID();

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.withdrawAccount(accountId),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_NOT_FOUND여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
        }

        @RepeatedTest(10)
        @DisplayName("OAuth 회원 탈퇴 시도, 인증되지 않은 회원")
        void withdrawAccount_invalidStatus_inactive() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.withdrawAccount(accountId),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_INACTIVE, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_INACTIVE여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
        }

        @RepeatedTest(10)
        @DisplayName("OAuth 회원 탈퇴 시도, 이미 탈퇴한 회원")
        void withdrawAccount_invalidStatus_alreadyWithdrawn() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.DELETED);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.withdrawAccount(accountId),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_ALREADY_WITHDRAWN, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_ALREADY_WITHDRAWN이여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
        }

        @RepeatedTest(10)
        @DisplayName("OAuth 회원 탈퇴 시도, 차단된 회원")
        void withdrawAccount_invalidStatus_blocked() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.BLOCKED);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.withdrawAccount(accountId),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_BLOCKED, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_BLOCKED여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
        }

        @RepeatedTest(10)
        @DisplayName("OAuth 회원 탈퇴 시도, OAuth 연동 정보가 없음")
        void withdrawAccount_emptyOAuthConnections() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.ACTIVE);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.withdrawAccount(accountId),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(OAUTH_PROVIDER_NOT_SUPPORTED, exception.getErrorCode(),
                                         "errorCode는 OAUTH_PROVIDER_NOT_SUPPORTED여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
        }

    }

    @Nested
    @DisplayName("withdrawAccountWithPassword() 테스트")
    class WithdrawAccountWithPasswordTests {

        @RepeatedTest(10)
        @DisplayName("이메일 회원 탈퇴")
        void withdrawAccountWithPassword() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.ACTIVE);
            String currentPassword = account.getPassword();

            AccountWithdrawRequest request = TestUtils.createAccountWithdrawRequest(currentPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(eq(request.getCurrentPassword()), eq(currentPassword))).thenReturn(true);

            // when
            accountService.withdrawAccount(accountId, request);

            // then
            assertEquals(AccountStatus.DELETED, account.getStatus(), "status는 DELETED로 설정되어야 합니다.");

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, times(1)).matches(eq(request.getCurrentPassword()), eq(currentPassword));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 탈퇴 시도, 해당 회원이 없음")
        void withdrawAccountWithPassword_notFoundAccount() {
            // given
            UUID accountId = UUID.randomUUID();

            String currentPassword = TestUtils.createPassword();

            AccountWithdrawRequest request = TestUtils.createAccountWithdrawRequest(currentPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.withdrawAccount(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_NOT_FOUND여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, never()).matches(eq(request.getCurrentPassword()), eq(currentPassword));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 탈퇴 시도, 비밀번호 불일치")
        void withdrawAccountWithPassword_invalidPassword() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.ACTIVE);
            String currentPassword = account.getPassword();

            AccountWithdrawRequest request = TestUtils.createAccountWithdrawRequest(currentPassword + ".");

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(eq(request.getCurrentPassword()), eq(currentPassword))).thenReturn(false);

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.withdrawAccount(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(INVALID_PASSWORD, exception.getErrorCode(),
                                         "errorCode는 INVALID_PASSWORD여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, times(1)).matches(eq(request.getCurrentPassword()), eq(currentPassword));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 탈퇴 시도, 미인증된 회원")
        void withdrawAccountWithPassword_invalidStatus_inactive() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            String currentPassword = account.getPassword();

            AccountWithdrawRequest request = TestUtils.createAccountWithdrawRequest(currentPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(eq(request.getCurrentPassword()), eq(currentPassword))).thenReturn(true);

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.withdrawAccount(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_INACTIVE, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_INACTIVE여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, times(1)).matches(eq(request.getCurrentPassword()), eq(currentPassword));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 탈퇴 시도, 이미 탈퇴한 회원")
        void withdrawAccountWithPassword_invalidStatus_alreadyWithdrawn() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.DELETED);
            String currentPassword = account.getPassword();

            AccountWithdrawRequest request = TestUtils.createAccountWithdrawRequest(currentPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(eq(request.getCurrentPassword()), eq(currentPassword))).thenReturn(true);

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.withdrawAccount(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_ALREADY_WITHDRAWN, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_ALREADY_WITHDRAWN이여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, times(1)).matches(eq(request.getCurrentPassword()), eq(currentPassword));
        }

        @RepeatedTest(10)
        @DisplayName("이메일 회원 탈퇴 시도, 차단된 회원")
        void withdrawAccountWithPassword_invalidStatus_blocked() {
            // given
            UUID accountId = UUID.randomUUID();

            Account account = TestUtils.createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            account.setStatus(AccountStatus.BLOCKED);
            String currentPassword = account.getPassword();

            AccountWithdrawRequest request = TestUtils.createAccountWithdrawRequest(currentPassword);

            when(accountRepository.findById(eq(accountId))).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(eq(request.getCurrentPassword()), eq(currentPassword))).thenReturn(true);

            // when
            CustomException exception = assertThrows(CustomException.class,
                                                     () -> accountService.withdrawAccount(accountId, request),
                                                     "CustomException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_BLOCKED, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_BLOCKED여야 합니다."));

            verify(accountRepository, times(1)).findById(eq(accountId));
            verify(passwordEncoder, times(1)).matches(eq(request.getCurrentPassword()), eq(currentPassword));
        }

    }

}
