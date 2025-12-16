package com.example.demo.domain.account.dao;

import com.example.demo.domain.account.dto.AccountResponse.AccountInfoResponse;
import com.example.demo.domain.account.dto.QAccountResponse_AccountInfoResponse;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.model.AccountStatus;
import com.example.demo.domain.account.model.QAccount;
import com.example.demo.domain.account.model.QOAuthConnection;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * PackageName : com.example.demo.domain.account.dao
 * FileName    : AccountRepositoryImpl
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : Account 엔티티 커스텀 DAO 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private static final QAccount         ACCOUNT          = QAccount.account;
    private static final QOAuthConnection OAUTH_CONNECTION = QOAuthConnection.oAuthConnection;

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * OAuth 제공자와 고유 식별자로 계정을 찾습니다.
     *
     * @param provider   - OAuth 제공자
     * @param providerId - OAuth 고유 식별자
     * @return 계정 엔티티
     */
    @Override
    public Optional<Account> findByProviderAndProviderId(final String provider, final String providerId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(ACCOUNT)
                                                  .join(ACCOUNT.oAuthConnections, OAUTH_CONNECTION)
                                                  .where(OAUTH_CONNECTION.provider.eq(provider),
                                                         OAUTH_CONNECTION.providerId.eq(providerId),
                                                         OAUTH_CONNECTION.deletedAt.isNull())
                                                  .fetchOne());
    }

    /**
     * 계정 ID로 계정 정보를 반환합니다.
     *
     * @param id - 계정 ID
     * @return 계정 정보 응답 DTO
     */
    @Override
    public Optional<AccountInfoResponse> getAccountInfoResponseById(final UUID id) {
        AccountInfoResponse content = jpaQueryFactory.select(
                                                             new QAccountResponse_AccountInfoResponse(ACCOUNT.id,
                                                                                                      ACCOUNT.email,
                                                                                                      ACCOUNT.nickname,
                                                                                                      ACCOUNT.role,
                                                                                                      ACCOUNT.status,
                                                                                                      Expressions.constant(new ArrayList<>()),
                                                                                                      ACCOUNT.createdAt,
                                                                                                      ACCOUNT.updatedAt)
                                                     )
                                                     .from(ACCOUNT)
                                                     .where(ACCOUNT.id.eq(id))
                                                     .fetchOne();

        if (content != null)
            content.addProviders(jpaQueryFactory.select(OAUTH_CONNECTION.provider)
                                                .from(OAUTH_CONNECTION)
                                                .where(OAUTH_CONNECTION.account.id.eq(id))
                                                .fetch());

        return Optional.ofNullable(content);
    }

    /**
     * 계정 ID, 계정 상태로 계정 정보를 반환합니다.
     *
     * @param id     - 계정 ID
     * @param status - 계정 상태
     * @return 계정 정보 응답 DTO
     */
    @Override
    public Optional<AccountInfoResponse> getAccountInfoResponseByIdAndStatus(final UUID id,
                                                                             final AccountStatus status) {
        AccountInfoResponse content = jpaQueryFactory.select(
                                                             new QAccountResponse_AccountInfoResponse(ACCOUNT.id,
                                                                                                      ACCOUNT.email,
                                                                                                      ACCOUNT.nickname,
                                                                                                      ACCOUNT.role,
                                                                                                      ACCOUNT.status,
                                                                                                      Expressions.constant(new ArrayList<>()),
                                                                                                      ACCOUNT.createdAt,
                                                                                                      ACCOUNT.updatedAt)
                                                     )
                                                     .from(ACCOUNT)
                                                     .where(ACCOUNT.id.eq(id), ACCOUNT.status.eq(status))
                                                     .fetchOne();

        if (content != null)
            content.addProviders(jpaQueryFactory.select(OAUTH_CONNECTION.provider)
                                                .from(OAUTH_CONNECTION)
                                                .where(OAUTH_CONNECTION.account.id.eq(id))
                                                .fetch());

        return Optional.ofNullable(content);
    }

}
