package com.example.demo.domain.account.dao;

import static com.example.demo.common.util.TestUtils.createAccount;
import static com.example.demo.common.util.TestUtils.createOAuthConnection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import autoparams.AutoSource;
import autoparams.Repeat;
import com.example.demo.common.config.EnableJpaAuditingConfig;
import com.example.demo.common.config.P6SpyConfig;
import com.example.demo.common.config.QuerydslConfig;
import com.example.demo.common.util.TestUtils;
import com.example.demo.domain.account.dto.AccountResponse.AccountInfoResponse;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.model.AccountStatus;
import com.example.demo.domain.account.model.OAuthConnection;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

/**
 * PackageName : com.example.demo.domain.account.dao
 * FileName    : AccountRepositoryTest
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : AccountRepository 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@DataJpaTest
@Import({EnableJpaAuditingConfig.class, P6SpyConfig.class, QuerydslConfig.class})
class AccountRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    AccountRepository accountRepository;

    @Nested
    @DisplayName("save() 테스트")
    class SaveTests {

        @RepeatedTest(10)
        @DisplayName("Account 엔티티 저장")
        void save() {
            // given
            Account account = createAccount();

            // when
            UUID id = accountRepository.save(account).getId();
            em.flush();

            // then
            Account savedAccount = em.find(Account.class, id);

            assertNotNull(savedAccount, "savedAccount는 null이 아니어야 합니다.");
            assertEquals(account.getEmail(), savedAccount.getEmail(), "email은 같아야 합니다.");
            assertEquals(account.getPassword(), savedAccount.getPassword(), "password는 같아야 합니다.");
            assertEquals(account.getNickname(), savedAccount.getNickname(), "nickname은 같아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 Account 엔티티 단 건 조회")
        void findById() {
            // given
            Account account = em.persistAndFlush(createAccount());
            UUID    id      = account.getId();

            // when
            Account findAccount = accountRepository.findById(id).get();

            // then
            assertNotNull(findAccount, "findAccount는 null이 아니어야 합니다.");
            assertEquals(account.getEmail(), findAccount.getEmail(), "email은 같아야 합니다.");
            assertEquals(account.getPassword(), findAccount.getPassword(), "password는 같아야 합니다.");
            assertEquals(account.getNickname(), findAccount.getNickname(), "nickname은 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id로 Account 엔티티 단 건 조회 시도")
        void findById_unknownId(final UUID unknownId) {
            // when
            Optional<Account> opAccount = accountRepository.findById(unknownId);

            // then
            assertFalse(opAccount.isPresent(), "조회된 Account 엔티티가 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("findByIdAndAccountStatus() 테스트")
    class FindByIdAndAccountStatusTests {

        @RepeatedTest(10)
        @DisplayName("id, accountStatus로 Account 엔티티 단 건 조회")
        void findByIdAndAccountStatus() {
            // given
            Account       account = em.persistAndFlush(createAccount());
            UUID          id      = account.getId();
            AccountStatus status  = account.getStatus();

            // when
            Account findAccount = accountRepository.findByIdAndStatus(id, status).get();

            // then
            assertNotNull(findAccount, "findAccount는 null이 아니어야 합니다.");
            assertEquals(account.getEmail(), findAccount.getEmail(), "email은 같아야 합니다.");
            assertEquals(account.getPassword(), findAccount.getPassword(), "password는 같아야 합니다.");
            assertEquals(account.getNickname(), findAccount.getNickname(), "nickname은 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id, accountStatus로 Account 엔티티 단 건 조회 시도")
        void findByIdAndAccountStatus_unknownId(final UUID unknownId, final AccountStatus status) {
            // when
            Optional<Account> opAccount = accountRepository.findByIdAndStatus(unknownId, status);

            // then
            assertFalse(opAccount.isPresent(), "조회된 Account 엔티티가 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("findByEmail() 테스트")
    class FindByEmailTests {

        @RepeatedTest(10)
        @DisplayName("Email로 Account 엔티티 단 건 조회")
        void findByEmail() {
            // given
            Account account = em.persistAndFlush(createAccount());
            String  email   = account.getEmail();

            // when
            Account findAccount = accountRepository.findByEmail(email).get();

            // then
            assertNotNull(findAccount, "findAccount는 null이 아니어야 합니다.");
            assertEquals(account.getEmail(), findAccount.getEmail(), "email은 같아야 합니다.");
            assertEquals(account.getPassword(), findAccount.getPassword(), "password는 같아야 합니다.");
            assertEquals(account.getNickname(), findAccount.getNickname(), "nickname은 같아야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("존재하지 않는 email로 Account 엔티티 단 건 조회 시도")
        void findByEmail_unknownEmail() {
            // given
            String unknownEmail = TestUtils.FAKER.internet().safeEmailAddress();

            // when
            Optional<Account> opAccount = accountRepository.findByEmail(unknownEmail);

            // then
            assertFalse(opAccount.isPresent(), "조회된 Account 엔티티가 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("findByEmailAndAccountStatusAndDeletedAtNull() 테스트")
    class FindByEmailAndAccountStatusAndDeletedAtNullTests {

        @RepeatedTest(10)
        @DisplayName("email, accountStatus로 deletedAt이 null인 Account 엔티티 단 건 조회")
        void findByEmailAndAccountStatusAndDeletedAtNull() {
            // given
            Account       account = em.persistAndFlush(createAccount());
            String        email   = account.getEmail();
            AccountStatus status  = account.getStatus();

            // when
            Account findAccount = accountRepository.findByEmailAndStatusAndDeletedAtNull(email, status).get();

            // then
            assertNotNull(findAccount, "findAccount는 null이 아니어야 합니다.");
            assertEquals(account.getEmail(), findAccount.getEmail(), "email은 같아야 합니다.");
            assertEquals(account.getPassword(), findAccount.getPassword(), "password는 같아야 합니다.");
            assertEquals(account.getNickname(), findAccount.getNickname(), "nickname은 같아야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("email, accountStatus로 deletedAt이 null이 아닌 Account 엔티티 단 건 조회 시도")
        void findByEmailAndAccountStatusAndDeletedAtNull_deletedAtNotNull() {
            // given
            Account account = createAccount();
            account.withdraw();
            String        email  = account.getEmail();
            AccountStatus status = account.getStatus();
            em.persistAndFlush(account);

            // when
            Optional<Account> opAccount = accountRepository.findByEmailAndStatusAndDeletedAtNull(email, status);

            // then
            assertFalse(opAccount.isPresent(), "조회된 Account 엔티티가 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("existsByIdAndAccountStatus() 테스트")
    class ExistsByIdAndAccountStatusTests {

        @RepeatedTest(10)
        @DisplayName("id, accountStatus로 Account 엔티티 존재 여부 확인")
        void existsByIdAndAccountStatus() {
            // given
            Account       account = em.persistAndFlush(createAccount());
            UUID          id      = account.getId();
            AccountStatus status  = account.getStatus();

            // when
            boolean exists = accountRepository.existsByIdAndStatus(id, status);

            // then
            assertTrue(exists, "Account 엔티티가 존재해야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id, accountStatus로 Account 엔티티 존재 여부 확인")
        void existsByIdAndAccountStatus_unknownId(final UUID unknownId, final AccountStatus status) {
            // when
            boolean exists = accountRepository.existsByIdAndStatus(unknownId, status);

            // then
            assertFalse(exists, "Account 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("existsByEmail() 테스트")
    class ExistsByEmailTests {

        @RepeatedTest(10)
        @DisplayName("email로 Account 엔티티 존재 여부 확인")
        void existsByEmail() {
            // given
            Account account = em.persistAndFlush(createAccount());
            String  email   = account.getEmail();

            // when
            boolean exists = accountRepository.existsByEmail(email);

            // then
            assertTrue(exists, "Account 엔티티가 존재해야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("존재하지 않는 email로 Account 엔티티 존재 여부 확인")
        void existsByEmail_unknownEmail() {
            // given
            String unknownEmail = TestUtils.FAKER.internet().safeEmailAddress();

            // when
            boolean exists = accountRepository.existsByEmail(unknownEmail);

            // then
            assertFalse(exists, "Account 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("existsByNickname() 테스트")
    class ExistsByNicknameTests {

        @RepeatedTest(10)
        @DisplayName("nickname으로 Account 엔티티 존재 여부 확인")
        void existsByNickname() {
            // given
            Account account  = em.persistAndFlush(createAccount());
            String  nickname = account.getNickname();

            // when
            boolean exists = accountRepository.existsByNickname(nickname);

            // then
            assertTrue(exists, "Account 엔티티가 존재해야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("존재하지 않는 nickname으로 Account 엔티티 존재 여부 확인")
        void existsByNickname_unknownNickname() {
            // given
            String unknownNickname = TestUtils.FAKER.credentials().username().replace(".", "").substring(0, 5);

            // when
            boolean exists = accountRepository.existsByNickname(unknownNickname);

            // then
            assertFalse(exists, "Account 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("엔티티 필드 업데이트 테스트")
    class DirtyCheckingTests {

        @RepeatedTest(10)
        @DisplayName("Account 엔티티 필드 업데이트")
        void update() {
            // given
            Account account        = em.persistAndFlush(createAccount());
            UUID    id             = account.getId();
            String  beforeNickname = account.getNickname();
            String  beforePassword = account.getPassword();

            // when
            Account findAccount = accountRepository.findById(id).get();
            findAccount.setNickname("updated" + findAccount.getNickname());
            findAccount.setPassword("updated" + findAccount.getPassword());

            // then
            Account updatedAccount = em.find(Account.class, id);

            assertNotNull(updatedAccount, "updatedAccount는 null이 아니어야 합니다.");
            assertEquals(findAccount.getNickname(), updatedAccount.getNickname(), "nickname은 업데이트된 값과 같아야 합니다.");
            assertNotEquals(beforeNickname, updatedAccount.getNickname(), "nickname은 업데이트 이전 값과 달라야 합니다.");
            assertEquals(findAccount.getPassword(), updatedAccount.getPassword(), "password는 업데이트된 값과 같아야 합니다.");
            assertNotEquals(beforePassword, updatedAccount.getPassword(), "password는 업데이트 이전 값과 달라야 합니다.");
        }

    }

    @Nested
    @DisplayName("deleteById() 테스트")
    class DeleteByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 Account 엔티티 삭제")
        void deleteById() {
            // given
            Account account = em.persistAndFlush(createAccount());
            UUID    id      = account.getId();

            // when
            accountRepository.deleteById(id);

            // then
            Account deletedAccount = em.find(Account.class, id);

            assertNull(deletedAccount, "deletedAccount는 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("deleteByEmail() 테스트")
    class DeleteByEmailTests {

        @RepeatedTest(10)
        @DisplayName("email로 Account 엔티티 삭제")
        void deleteByEmail() {
            // given
            Account account = em.persistAndFlush(createAccount());
            String  email   = account.getEmail();
            UUID    id      = account.getId();

            // when
            accountRepository.deleteByEmail(email);

            // then
            Account deletedAccount = em.find(Account.class, id);

            assertNull(deletedAccount, "deletedAccount는 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("findByProviderAndProviderId() 테스트")
    class FindByProviderAndProviderIdTests {

        @RepeatedTest(10)
        @DisplayName("OAuth provider, providerId로 Account 엔티티 단 건 조회")
        void findByProviderAndProviderId() {
            // given
            Account         account         = em.persistAndFlush(createAccount());
            OAuthConnection oAuthConnection = em.persistAndFlush(createOAuthConnection(account));
            String          provider        = oAuthConnection.getProvider();
            String          providerId      = oAuthConnection.getProviderId();

            // when
            Account findAccount = accountRepository.findByProviderAndProviderId(provider, providerId).get();

            // then
            OAuthConnection findOAuthConnection = findAccount.getOAuthConnections().getFirst();

            assertNotNull(findAccount, "findAccount는 null이 아니어야 합니다.");
            assertEquals(findOAuthConnection.getProvider(), provider, "provider는 같아야 합니다.");
            assertEquals(findOAuthConnection.getProviderId(), providerId, "providerId는 같아야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("존재하지 않는 OAuth provider, providerId로 Account 엔티티 단 건 조회 시도")
        void findByProviderAndProviderId_unknownProviderAndProviderId() {
            // given
            String unknownProvider   = TestUtils.FAKER.company().name();
            String unknownProviderId = UUID.randomUUID().toString();

            // when
            Optional<Account> opAccount = accountRepository.findByProviderAndProviderId(
                    unknownProvider, unknownProviderId
            );

            // then
            assertFalse(opAccount.isPresent(), "조회된 Account 엔티티가 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("getAccountInfoResponseById() 테스트")
    class GetAccountInfoResponseByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 AccountInfoResponse DTO 단 건 조회")
        void getAccountInfoResponseById() {
            // given
            Account         account         = em.persistAndFlush(createAccount());
            OAuthConnection oAuthConnection = em.persistAndFlush(createOAuthConnection(account));
            UUID            id              = account.getId();

            // when
            AccountInfoResponse findAccountInfoResponse = accountRepository.getAccountInfoResponseById(id).get();

            // then
            OAuthConnection findOAuthConnection = account.getOAuthConnections().getFirst();

            assertNotNull(findAccountInfoResponse, "findAccountInfoResponse는 null이 아니어야 합니다.");
            assertEquals(account.getEmail(), findAccountInfoResponse.getEmail(), "email은 같아야 합니다.");
            assertEquals(account.getNickname(), findAccountInfoResponse.getNickname(), "nickname은 같아야 합니다.");
            assertEquals(account.getRole(), findAccountInfoResponse.getRole(), "role은 같아야 합니다.");
            assertEquals(account.getStatus(), findAccountInfoResponse.getStatus(), "status는 같아야 합니다.");
            assertEquals(findOAuthConnection.getProvider(), oAuthConnection.getProvider(), "provider는 같아야 합니다.");
            assertEquals(findOAuthConnection.getProviderId(), oAuthConnection.getProviderId(), "providerId는 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id로 AccountInfoResponse DTO 단 건 조회 시도")
        void getAccountInfoResponseById_unknownId(final UUID unknownId) {
            // when
            Optional<AccountInfoResponse> opAccountInfoResponse =
                    accountRepository.getAccountInfoResponseById(unknownId);

            // then
            assertTrue(opAccountInfoResponse.isEmpty(), "조회된 AccountInfoRespponse DTO가 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("getAccountInfoResponseByIdAndStatus() 테스트")
    class GetAccountInfoResponseByIdAndStatusTests {

        @RepeatedTest(10)
        @DisplayName("id, accountStatus로 AccountInfoResponse DTO 단 건 조회")
        void getAccountInfoResponseByIdAndStatus() {
            // given
            Account         account         = em.persistAndFlush(createAccount());
            OAuthConnection oAuthConnection = em.persistAndFlush(createOAuthConnection(account));
            UUID            id              = account.getId();
            AccountStatus   status          = account.getStatus();

            // when
            AccountInfoResponse findAccountInfoResponse = accountRepository.getAccountInfoResponseByIdAndStatus(
                                                                                   id, status
                                                                           )
                                                                           .get();

            // then
            OAuthConnection findOAuthConnection = account.getOAuthConnections().getFirst();

            assertNotNull(findAccountInfoResponse, "findAccountInfoResponse는 null이 아니어야 합니다.");
            assertEquals(account.getEmail(), findAccountInfoResponse.getEmail(), "email은 같아야 합니다.");
            assertEquals(account.getNickname(), findAccountInfoResponse.getNickname(), "nickname은 같아야 합니다.");
            assertEquals(account.getRole(), findAccountInfoResponse.getRole(), "role은 같아야 합니다.");
            assertEquals(account.getStatus(), findAccountInfoResponse.getStatus(), "status는 같아야 합니다.");
            assertEquals(findOAuthConnection.getProvider(), oAuthConnection.getProvider(), "provider는 같아야 합니다.");
            assertEquals(findOAuthConnection.getProviderId(), oAuthConnection.getProviderId(), "providerId는 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id, accountStatus로 AccountInfoResponse DTO 단 건 조회 시도")
        void getAccountInfoResponseByIdAndStatus_unknownId(final UUID unknownId, final AccountStatus status) {
            // when
            Optional<AccountInfoResponse> opAccountInfoResponse = accountRepository.getAccountInfoResponseByIdAndStatus(
                    unknownId, status
            );

            // then
            assertTrue(opAccountInfoResponse.isEmpty(), "조회된 AccountInfoRespponse DTO가 null이어야 합니다.");
        }

    }

}
