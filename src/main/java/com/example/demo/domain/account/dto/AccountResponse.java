package com.example.demo.domain.account.dto;

import static lombok.AccessLevel.PRIVATE;

import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.model.AccountRole;
import com.example.demo.domain.account.model.AccountStatus;
import com.example.demo.domain.account.model.OAuthConnection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.account.dto
 * FileName    : AccountResponse
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 계정 도메인 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(name = "계정 도메인 응답 DTO")
public abstract class AccountResponse {

    @Getter
    @Schema(name = "계정 정보 응답 DTO")
    public static class AccountInfoResponse {

        @Schema(name = "계정 ID")
        private final UUID          id;
        @Schema(name = "이메일 주소(아이디)")
        private final String        email;
        @Schema(name = "닉네임")
        private final String        nickname;
        @Schema(name = "계정 역할: [USER, ADMIN]")
        private final AccountRole   role;
        @Schema(name = "계정 상태: [ACTIVE, INACTIVE, DELETED, BLOCKED]")
        private final AccountStatus status;
        @Schema(name = "OAuth 인증 제공자 목록")
        private final List<String>  providers;
        @JsonProperty("created_at")
        @Schema(name = "생성 일시")
        private final LocalDateTime createdAt;
        @JsonProperty("updated_at")
        @Schema(name = "수정 일시")
        private final LocalDateTime updatedAt;

        @QueryProjection
        public AccountInfoResponse(@JsonProperty("id") final UUID id,
                                   @JsonProperty("email") final String email,
                                   @JsonProperty("nickname") final String nickname,
                                   @JsonProperty("role") final AccountRole role,
                                   @JsonProperty("status") final AccountStatus status,
                                   @JsonProperty("providers") final List<String> providers,
                                   @JsonProperty("created_at") final LocalDateTime createdAt,
                                   @JsonProperty("updated_at") final LocalDateTime updatedAt) {
            this.id = id;
            this.email = email;
            this.nickname = nickname;
            this.role = role;
            this.status = status;
            this.providers = providers;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public static AccountInfoResponse from(final Account account) {
            if (account == null) return null;
            return new AccountInfoResponse(account.getId(),
                                           account.getEmail(),
                                           account.getNickname(),
                                           account.getRole(),
                                           account.getStatus(),
                                           account.getOAuthConnections().
                                                  stream()
                                                  .map(OAuthConnection::getProvider)
                                                  .toList(),
                                           account.getCreatedAt(),
                                           account.getUpdatedAt());
        }

        public void addProviders(final List<String> providers) {
            this.providers.addAll(providers);
        }

    }

}
