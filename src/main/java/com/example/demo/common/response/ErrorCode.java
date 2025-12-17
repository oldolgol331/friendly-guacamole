package com.example.demo.common.response;

import static lombok.AccessLevel.PRIVATE;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * PackageName : com.example.demo.common.response
 * FileName    : ErrorCode
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 공통 응답 예외 코드
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum ErrorCode {

    // 공통(Common)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "CO001", "유효하지 않은 입력 값입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "CO002", "유효하지 않은 타입 값입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "CO003", "해당 엔티티를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "CO004", "지원하지 않는 HTTP Method 입니다."),
    METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "CO005", "지원하지 않는 Content-Type 입니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "CO006", "요청 인자의 타입이 올바르지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "CO007", "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "CO008", "인증 정보가 유효하지 않습니다."),
    CONSTRAINT_VIOLATION(HttpStatus.CONFLICT, "CO009", "데이터베이스 제약 조건 위반입니다."),
    BAD_SQL_GRAMMAR(HttpStatus.INTERNAL_SERVER_ERROR, "CO010", "잘못된 SQL 문법 오류가 발생했습니다."),
    REQUEST_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "CO011", "요청의 크기가 너무 큽니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "CO012", "너무 많은 요청을 보냈습니다. 잠시 후 다시 시도해주세요."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "CO013", "요청하신 리소스를 찾을 수 없습니다."),
    MISSING_INPUT_VALUE(HttpStatus.BAD_REQUEST, "CO014", "필수 입력값이 누락되었습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CO999", "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."),

    // 계정(Account) & 인증(Auth)
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "AC001", "해당 계정을 찾을 수 없습니다."),
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "AC002", "이미 사용 중인 이메일입니다."),
    NICKNAME_DUPLICATION(HttpStatus.CONFLICT, "AC003", "이미 사용 중인 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "AC004", "비밀번호가 일치하지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AC005", "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "AC006", "비활성화된 계정입니다. 관리자에게 문의하세요."),
    ACCOUNT_BLOCKED(HttpStatus.FORBIDDEN, "AC007", "차단된 계정입니다. 관리자에게 문의하세요."),
    ACCOUNT_ALREADY_WITHDRAWN(HttpStatus.FORBIDDEN, "AC008", "이미 탈퇴 처리된 계정입니다. 관리자에게 문의하세요."),
    INVALID_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, "AC009", "유효하지 않은 인증 토큰입니다."),
    ALREADY_VERIFIED_EMAIL(HttpStatus.BAD_REQUEST, "AC010", "이미 인증된 이메일입니다."),
    PASSWORD_CHANGE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "AC011", "비밀번호 변경을 지원하지 않는 인증 타입입니다."),
    OAUTH_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "AC012", "지원하지 않는 OAuth 제공자입니다."),
    OAUTH_USER_CANNOT_RESET_PASSWORD(HttpStatus.BAD_REQUEST, "AC013", "소셜 로그인 계정은 비밀번호 초기화를 할 수 없습니다."),
    AUTH_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "AC014", "지원하지 않는 인증 타입입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AC015", "JWT AccessToken이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AC016", "JWT RefreshToken이 유효하지 않습니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AC017", "JWT AccessToken이 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AC018", "JWT RefreshToken이 만료되었습니다."),
    TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AC019", "JWT 토큰이 일치하지 않습니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "AC020", "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "AC021", "비밀번호 형식이 올바르지 않습니다. (8~20자, 영문 대문자/소문자/숫자/특수문자 포함)"),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "AC022", "닉네임 형식이 올바르지 않습니다. (2~15자, 특수문자 제외"),
    OAUTH_PASSWORD_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "AC023", "소셜 로그인 계정은 비밀번호를 변경할 수 없습니다."),

    // 공연(Performance) & 좌석(Seat) & 예매(Reservation)
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "PM001", "해당 공연을 찾을 수 없습니다."),
    INVALID_PERFORMANCE_DATE(HttpStatus.BAD_REQUEST, "PM002", "공연 날짜가 유효하지 않습니다."),
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "ST001", "해당 좌석을 찾을 수 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "ST002", "이미 예약된 좌석입니다."),
    SEAT_ALREADY_SOLD(HttpStatus.CONFLICT, "ST003", "이미 판매된 좌석입니다."),
    SEAT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "ST004", "예약 가능한 상태가 아닙니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RS001", "해당 예매 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String     code;
    private final String     message;

}
