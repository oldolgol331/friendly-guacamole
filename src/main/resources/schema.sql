SET
FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS oauth_connections CASCADE;
DROP TABLE IF EXISTS performances CASCADE;
DROP TABLE IF EXISTS seats CASCADE;
DROP TABLE IF EXISTS reservations CASCADE;

SET
FOREIGN_KEY_CHECKS = 1;

CREATE TABLE accounts
(
    account_id BINARY(16)   NOT NULL COMMENT '계정 고유 식별자',
    email      VARCHAR(255) NOT NULL COMMENT '계정 이메일',
    password   VARCHAR(255) NULL COMMENT '암호화된 비밀번호 (OAuth2 회원은 NULL)',
    nickname   VARCHAR(255) NOT NULL COMMENT '계정 닉네임',
    role       VARCHAR(255) NOT NULL DEFAULT 'USER' COMMENT '계정 권한 (USER, ADMIN)',
    status     VARCHAR(255) NOT NULL DEFAULT 'INACTIVE' COMMENT '계정 상태 (INACTIVE, ACTIVE, DELETED, BLOCKED)',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    deleted_at DATETIME NULL COMMENT '삭제 일시',
    CONSTRAINT PK_accounts PRIMARY KEY (account_id),
    CONSTRAINT UK_accounts_email UNIQUE (email),
    CONSTRAINT UK_accounts_nickname UNIQUE (nickname)
) COMMENT '계정 테이블';

CREATE TABLE oauth_connections
(
    oauth_connection_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'OAuth 연결 고유 식별자',
    account_id          BINARY(16)      NOT NULL COMMENT '연결된 계정 ID',
    provider            VARCHAR(255) NOT NULL COMMENT 'OAuth2 제공자 (GOOGLE, NAVER, KAKAO)',
    provider_id         VARCHAR(255) NOT NULL COMMENT 'OAuth2 제공자가 발급한 고유 식별자',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    deleted_at          DATETIME NULL COMMENT '삭제 일시',
    CONSTRAINT PK_oauth_connections PRIMARY KEY (oauth_connection_id),
    CONSTRAINT FK_oauth_connections_accounts FOREIGN KEY (account_id) REFERENCES accounts (account_id),
    CONSTRAINT UK_oauth_connections_provider_provider_id UNIQUE (provider, provider_id),
    CONSTRAINT UK_oauth_connections_account_id_provider UNIQUE (account_id, provider)
) COMMENT 'OAuth 연결 테이블';

CREATE TABLE performances
(
    performance_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '공연 고유 식별자',
    name           VARCHAR(255) NOT NULL COMMENT '공연 명칭',
    venue          VARCHAR(255) NOT NULL COMMENT '공연 장소',
    info           TEXT COMMENT '공연 정보',
    start_time     DATETIME     NOT NULL COMMENT '공연 시작 시간',
    end_time       DATETIME     NOT NULL COMMENT '공연 종료 시간',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    CONSTRAINT PK_performances PRIMARY KEY (performance_id),
    FULLTEXT INDEX IDX_fulltext_performances_name_info (name, info) WITH PARSER ngram
) COMMENT '공연 테이블';

CREATE TABLE seats
(
    seat_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '좌석 고유 식별자',
    performance_id BIGINT UNSIGNED NOT NULL COMMENT '좌석이 포함된 공연 식별자',
    seat_code      VARCHAR(255) NOT NULL COMMENT '좌석 번호',
    price          INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '좌석 가격',
    status         VARCHAR(255) NOT NULL DEFAULT 'AVAILABLE' COMMENT '좌석 상태 (AVAILABLE, RESERVED, SOLD)',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    version        BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '버전',
    CONSTRAINT PK_seats PRIMARY KEY (seat_id),
    CONSTRAINT FK_seats_performances FOREIGN KEY (performance_id) REFERENCES performances (performance_id)
) COMMENT '좌석 테이블';

CREATE TABLE reservations
(
    account_id       BINARY(16)      NOT NULL COMMENT '예약한 계정 식별자',
    seat_id          BIGINT UNSIGNED NOT NULL COMMENT '예약한 좌석 식별자',
    reservation_time DATETIME NOT NULL COMMENT '예약 확정 시간',
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    CONSTRAINT PK_reservations PRIMARY KEY (account_id, seat_id),
    CONSTRAINT FK_reservations_accounts FOREIGN KEY (account_id) REFERENCES accounts (account_id),
    CONSTRAINT FK_reservations_seats FOREIGN KEY (seat_id) REFERENCES seats (seat_id)
) COMMENT '예약 테이블';