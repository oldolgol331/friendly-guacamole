package com.example.demo.domain.account.dao;

import static com.example.demo.common.util.TestUtils.createAccount;
import static com.example.demo.common.util.TestUtils.createOAuthConnection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import autoparams.AutoSource;
import autoparams.Repeat;
import com.example.demo.common.config.EnableJpaAuditingConfig;
import com.example.demo.common.config.P6SpyConfig;
import com.example.demo.common.config.QuerydslConfig;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.model.OAuthConnection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Optional;
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
 * FileName    : OAuthConnectionRepositoryTest
 * Author      : oldolgol331
 * Date        : 25. 12. 22.
 * Description : OAuthConnectionRepository 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 22.   oldolgol331          Initial creation
 */
@DataJpaTest
@Import({EnableJpaAuditingConfig.class, P6SpyConfig.class, QuerydslConfig.class})
class OAuthConnectionRepositoryTest {

    @Autowired
    TestEntityManager         em;
    @Autowired
    OAuthConnectionRepository oAuthConnectionRepository;

    @Nested
    @DisplayName("save() 테스트")
    class SaveTests {

        @RepeatedTest(10)
        @DisplayName("OAuthConnection 엔티티 저장")
        void save() {
            // given
            Account         account         = em.persistAndFlush(createAccount());
            OAuthConnection oAuthConnection = createOAuthConnection(account);

            // when
            Long id = oAuthConnectionRepository.save(oAuthConnection).getId();
            em.flush();

            // then
            OAuthConnection savedOAuthConnection = em.find(OAuthConnection.class, id);

            assertNotNull(savedOAuthConnection, "savedOAuthConnection은 null이 아니어야 합니다.");
            assertEquals(oAuthConnection.getProvider(), savedOAuthConnection.getProvider(), "provider가 같아야 합니다.");
            assertEquals(oAuthConnection.getProviderId(), savedOAuthConnection.getProviderId(), "providerId는 같아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 OAuthConnection 엔티티 단 건 조회")
        void findById() {
            // given
            Account         account         = em.persistAndFlush(createAccount());
            OAuthConnection oAuthConnection = em.persistAndFlush(createOAuthConnection(account));
            Long            id              = oAuthConnection.getId();

            // when
            OAuthConnection findOAuthConnection = oAuthConnectionRepository.findById(id).get();

            // then
            assertNotNull(findOAuthConnection, "findOAuthConnection은 null이 아니어야 합니다.");
            assertEquals(oAuthConnection.getProvider(), findOAuthConnection.getProvider(), "provider가 같아야 합니다.");
            assertEquals(oAuthConnection.getProviderId(), findOAuthConnection.getProviderId(), "providerId는 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id로 OAuthConneciton 엔티티 단 건 조회 시도")
        void findById_unknownId(@Min(1) @Max(Long.MAX_VALUE) final Long unknownId) {
            // when
            Optional<OAuthConnection> opOAuthConnection = oAuthConnectionRepository.findById(unknownId);

            // then
            assertFalse(opOAuthConnection.isPresent(), "조회된 OAuthConnection 엔티티가 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("findByProviderAndProviderId() 테스트")
    class FindByProviderAndProviderIdTests {

        @RepeatedTest(10)
        @DisplayName("OAuth 제공자와 OAuth 제공 ID로 OAuthConnection 엔티티 단 건 조회")
        void findByProviderAndProviderId() {
            // given
            Account         account         = em.persistAndFlush(createAccount());
            OAuthConnection oAuthConnection = em.persistAndFlush(createOAuthConnection(account));
            String          provider        = oAuthConnection.getProvider();
            String          providerId      = oAuthConnection.getProviderId();

            // when
            OAuthConnection findOAuthConnection = oAuthConnectionRepository.findByProviderAndProviderId(provider,
                                                                                                        providerId)
                                                                           .get();

            // then
            assertNotNull(findOAuthConnection, "findOAuthConnection은 null이 아니어야 합니다.");
            assertEquals(oAuthConnection.getProvider(), findOAuthConnection.getProvider(), "provider가 같아야 합니다.");
            assertEquals(oAuthConnection.getProviderId(), findOAuthConnection.getProviderId(), "providerId는 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 OAuth 제공자와 OAuth 제공자 ID로 OAuthConnection 엔티티 단 건 조회 시도")
        void findByProviderAndProviderId_unknownProvider(final String unknownProvider, final String unknownProviderId) {
            // when
            Optional<OAuthConnection> opOAuthConnection =
                    oAuthConnectionRepository.findByProviderAndProviderId(unknownProvider,
                                                                          unknownProviderId);

            // then
            assertFalse(opOAuthConnection.isPresent(), "조회된 OAuthConnection 엔티티가 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("deleteById() 테스트")
    class DeleteByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 OAuthConnection 엔티티 삭제")
        void deleteById() {
            // given
            Account         account         = em.persistAndFlush(createAccount());
            OAuthConnection oAuthConnection = em.persistAndFlush(createOAuthConnection(account));
            Long            id              = oAuthConnection.getId();

            // when
            oAuthConnectionRepository.deleteById(id);

            // then
            OAuthConnection deletedOAuthConnection = em.find(OAuthConnection.class, id);

            assertNull(deletedOAuthConnection, "deletedOAuthConnection은 null이어야 합니다.");
        }

    }

}
