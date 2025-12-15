package com.example.demo.domain.account.model;

import static com.example.demo.domain.account.model.AccountRole.USER;
import static com.example.demo.domain.account.model.AccountStatus.ACTIVE;
import static com.example.demo.domain.account.model.AccountStatus.BLOCKED;
import static com.example.demo.domain.account.model.AccountStatus.DELETED;
import static com.example.demo.domain.account.model.AccountStatus.INACTIVE;
import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import com.example.demo.common.model.BaseAuditingEntity;
import com.example.demo.domain.account.constant.AccountConst;
import com.example.demo.domain.reservation.model.Reservation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * PackageName : com.example.demo.domain.account.model
 * FileName    : Account
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 계정 정보 엔티티
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Entity
@Table(name = "accounts",
       uniqueConstraints = {@UniqueConstraint(name = "UK_accounts_email", columnNames = "email"),
                            @UniqueConstraint(name = "UK_accounts_nickname", columnNames = "nickname")})
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Account extends BaseAuditingEntity {

    @Id
    @GeneratedValue(generator = "ulid_generator")
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "account_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;                                                    // ID

    @Column(nullable = false, updatable = false)
    @NotBlank
    private String email;                                               // 이메일

    private String password;                                            // 비밀번호

    @Column(nullable = false)
    @NotBlank
    private String nickname;                                            // 닉네임

    @Enumerated(STRING)
    @Column(nullable = false)
    @NotNull
    private AccountRole role;                                           // 계정 권한: USER(일반 계정0, ADMIN(관리자 계정)

    @Enumerated(STRING)
    @Column(nullable = false)
    @NotNull
    private AccountStatus status;                                       // 계정 상태: INACTIVE(비활성), ACTIVE(활성), DELETED(탈퇴함), BLOCKED(차단됨)

    private LocalDateTime deletedAt = null;                             // 탈퇴 일시

    @OneToMany(mappedBy = "account", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<OAuthConnection> oAuthConnections = new ArrayList<>(); // OAuth 연동 정보 목록

    @OneToMany(mappedBy = "account", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();         // 예약 정보 목록

    private Account(final String email,
                    final String nickname,
                    final AccountRole role,
                    final AccountStatus status) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
    }

    private Account(final String email,
                    final String password,
                    final String nickname,
                    final AccountRole role,
                    final AccountStatus status) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
    }

    // ========================= 생성자 메서드 =========================

    /**
     * Account 객체 생성
     *
     * @param email - 이메일
     * @param nickname - 닉네임
     * @param provider - OAuth2 제공자
     * @param providerId - OAuth2 고유 식별자
     * @return Account 객체
     */
    public static Account of(final String email,
                             final String nickname,
                             final String provider,
                             final String providerId) {
        return of(email, nickname, provider, providerId, USER, ACTIVE);
    }

    /**
     * Account 객체 생성
     *
     * @param email - 이메일
     * @param password - 비밀번호
     * @param nickname - 닉네임
     * @return Account 객체
     */
    public static Account of(final String email, final String password, final String nickname) {
        return of(email, password, nickname, USER, INACTIVE);
    }

    /**
     * Account 객체 생성
     *
     * @param email - 이메일
     * @param nickname - 닉네임
     * @param provider - OAuth2 제공자
     * @param providerId - OAuth2 고유 식별자
     * @param role - 계정 권한
     * @param status - 계정 상태
     * @return Account 객체
     */
    public static Account of(final String email,
                             final String nickname,
                             final String provider,
                             final String providerId,
                             final AccountRole role,
                             final AccountStatus status) {
        if (provider == null || provider.isBlank() || providerId == null || providerId.isBlank())
            throw new IllegalArgumentException("OAuth 제공자 및 고유 식별자는 필수입니다.");
        validateEmail(email);
        validateNickname(nickname);
        return new Account(email.toLowerCase(), nickname, role, status);
    }

    /**
     * Account 객체 생성
     *
     * @param email - 이메일
     * @param password - 비밀번호
     * @param nickname - 닉네임
     * @param role - 계정 권한
     * @param status - 계정 상태
     * @return Account 객체
     */
    public static Account of(final String email,
                             final String password,
                             final String nickname,
                             final AccountRole role,
                             final AccountStatus status) {
        validateEmail(email);
        validatePassword(password);
        validateNickname(nickname);
        return new Account(email.toLowerCase(), password, nickname, role, status);
    }

    // ========================= 검증 메서드 =========================

    /**
     * 이메일 유효성을 검사합니다.
     *
     * @param input - 입력값
     */
    private static void validateEmail(final String input) {
        if (input == null || !AccountConst.EMAIL_PATTERN.matcher(input).matches())
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
    }

    /**
     * 비밀번호 유효성을 검사합니다.
     *
     * @param input - 입력값
     */
    private static void validatePassword(final String input) {
        if (input == null || input.isBlank())
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        if (!AccountConst.PASSWORD_PATTERN.matcher(input).matches())
            throw new IllegalArgumentException("비밀번호 형식이 올바르지 않습니다. 8~20자, 영문 대/소문자, 특수문자 각각 최소 1개 이상 포함되어야 합니다.");
    }

    /**
     * 닉네임 유효성을 검사합니다.
     *
     * @param input - 입력값
     */
    private static void validateNickname(final String input) {
        if (input == null || input.isBlank())
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        if (!AccountConst.NICKNAME_PATTERN.matcher(input).matches())
            throw new IllegalArgumentException("닉네임 형식이 올바르지 않습니다. 2~15자 영문, 한글, 숫자, '-', '_'만 가능합니다.");
    }

    // ========================= JPA 콜백 메서드 =========================

    /**
     * 이메일을 소문자로 변환하여 저장합니다.
     */
    @PrePersist
    @PreUpdate
    private void convertEmailToLowerCase() {
        if (email != null) email = email.toLowerCase();
    }

    // ========================= 비즈니스 메서드 =========================

    /**
     * 비밀번호를 변경합니다.
     *
     * @param input - 입력값
     */
    public void setPassword(final String input) {
        if (password == null || password.isBlank())
            throw new IllegalStateException("OAuth 연동 생성 계정은 비밀번호를 변경할 수 없습니다.");
        validatePassword(input);
        password = input;
    }

    /**
     * 닉네임을 변경합니다.
     *
     * @param input - 입력값
     */
    public void setNickname(final String input) {
        validateNickname(input);
        nickname = input;
    }

    /**
     * 계정 권한을 변경합니다.
     *
     * @param input - 입력값
     */
    public void setRole(final AccountRole input) {
        if (input == null) throw new IllegalArgumentException("계정 권한은 필수입니다.");
        role = input;
    }

    /**
     * 계정 상태를 변경합니다.
     *
     * @param input - 입력값
     */
    public void setStatus(final AccountStatus input) {
        if (input == null) throw new IllegalArgumentException("계정 상태는 필수입니다.");
        if (status == DELETED && input != DELETED) deletedAt = null;
        status = input;
    }

    /**
     * 이메일 인증 완료 시 계정 상태를 활성화합니다.
     */
    public void completeEmailVerification() {
        switch (status) {
            case INACTIVE -> status = ACTIVE;
            case ACTIVE -> throw new IllegalStateException("이미 인증된 계정입니다.");
            case DELETED -> throw new IllegalStateException("탈퇴한 계정입니다.");
            case BLOCKED -> throw new IllegalStateException("차단된 회원입니다.");
        }
    }

    /**
     * 계정 상태를 차단으로 변경합니다.
     */
    public void block() {
        status = BLOCKED;
    }

    /**
     * 계정 상태를 탈퇴로 변경합니다. 계정의 탈퇴일을 현재 시간으로 설정합니다.
     */
    public void withdraw() {
        if (deletedAt != null || status == DELETED) throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        status = DELETED;
        deletedAt = LocalDateTime.now();
    }

}
