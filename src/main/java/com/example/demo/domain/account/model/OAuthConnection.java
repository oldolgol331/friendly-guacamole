package com.example.demo.domain.account.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import com.example.demo.common.model.BaseAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.account.model
 * FileName    : OAuthConnection
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : OAuth 연동 정보 엔티티
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Entity
@Table(name = "oauth_connections",
       uniqueConstraints = {@UniqueConstraint(name = "UK_oauth_connections_provider_provider_id",
                                              columnNames = {"provider", "provider_id"}),
                            @UniqueConstraint(name = "UK_oauth_connections_account_id_provider",
                                              columnNames = {"account_id", "provider"})})
@Getter
@NoArgsConstructor(access = PROTECTED)
public class OAuthConnection extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "oauth_connection_id", nullable = false, updatable = false)
    private Long id;                        // ID

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id",
                nullable = false,
                updatable = false,
                foreignKey = @ForeignKey(name = "FK_oauth_connections_accounts"))
    private Account account;                // 계정 엔티티

    @Column(nullable = false, updatable = false)
    @NotBlank
    private String provider;                // OAuth2 제공자

    @Column(nullable = false, updatable = false)
    @NotBlank
    private String providerId;              // OAuth2 고유 식별자

    private LocalDateTime deletedAt = null; // 삭제 일시

    private OAuthConnection(final String provider, final String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }

    // ========================= 생성자 메서드 =========================

    /**
     * OAuthConnection 객체 생성
     *
     * @param account    - 계정
     * @param provider   - OAuth2 제공자
     * @param providerId - OAuth2 고유 식별자
     * @return OAuthConnection 객체
     */
    public static OAuthConnection of(final Account account, final String provider, final String providerId) {
        OAuthConnection oAuthConnection = new OAuthConnection(provider, providerId);
        oAuthConnection.setRelationshipWithAccount(account);
        return oAuthConnection;
    }

    // ========================= 연관관계 메서드 =========================

    /**
     * 계정과의 관계를 설정합니다.
     *
     * @param account - 계정
     */
    private void setRelationshipWithAccount(final Account account) {
        this.account = account;
        account.getOAuthConnections().add(this);
    }

    // ========================= 비즈니스 메서드 =========================

    /**
     * OAuth 연동 정보를 삭제 처리합니다. OAuth 연동 정보의 삭제일시를 현재 시간으로 설정합니다.
     */
    public void delete() {
        if (deletedAt != null) throw new IllegalStateException("이미 삭제된 OAuth 연동 정보입니다.");
        deletedAt = LocalDateTime.now();
    }

}
