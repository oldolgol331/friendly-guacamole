package com.example.demo.common.response;

import static lombok.AccessLevel.PRIVATE;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * PackageName : com.example.demo.common.response
 * FileName    : SuccessCode
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 공통 응답 성공 코드
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum SuccessCode {

    // 공통(Common)
    REQUEST_SUCCESS(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    CREATE_SUCCESS(HttpStatus.CREATED, "성공적으로 생성되었습니다."),
    UPDATE_SUCCESS(HttpStatus.OK, "성공적으로 업데이트되었습니다."),
    DELETE_SUCCESS(HttpStatus.OK, "성공적으로 삭제되었습니다."),

    // 계정(Account) & 인증(Auth)
    ACCOUNT_REGISTER_SUCCESS(HttpStatus.CREATED, "회원가입이 성공적으로 완료되었습니다."),
    ACCOUNT_LOGIN_SUCCESS(HttpStatus.OK, "로그인에 성공했습니다."),
    ACCOUNT_LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃에 성공했습니다."),
    AUTHENTICATION_TOKEN_RENEW_SUCCESS(HttpStatus.OK, "인증 토큰이 성공적으로 갱신되었습니다."),
    ACCOUNT_INFO_FETCH_SUCCESS(HttpStatus.OK, "계정 정보를 성공적으로 조회했습니다."),
    EMAIL_VERIFICATION_SUCCESS(HttpStatus.OK, "이메일 인증이 성공적으로 완료되었습니다."),
    EMAIL_SENT(HttpStatus.OK, "이메일이 발송되었습니다."),
    UPDATE_ACCOUNT_INFO_SUCCESS(HttpStatus.OK, "계정 정보를 성공적으로 수정했습니다."),
    PASSWORD_CHANGED_SUCCESS(HttpStatus.OK, "비밀번호가 성공적으로 변경되었습니다."),
    ACCOUNT_WITHDRAWN_SUCCESS(HttpStatus.OK, "회원 탈퇴가 성공적으로 처리되었습니다."),

    // 공연(Performance) & 좌석(Seat)
    PERFORMANCE_CREATE_SUCCESS(HttpStatus.CREATED, "공연 생성이 성공적으로 완료되었습니다."),
    PERFORMANCE_LIST_SEARCH_SUCCESS(HttpStatus.OK, "공연 목록 조회가 성공적으로 완료되었습니다."),
    PERFORMANCE_READ_SUCCESS(HttpStatus.OK, "공연 상세 조회가 성공적으로 완료되었습니다."),
    UPDATE_PERFORMANCE_INFO_SUCCESS(HttpStatus.OK, "공연 정보를 성공적으로 수정했습니다."),
    DELETE_PERFORMANCE_SUCCESS(HttpStatus.OK, "공연 삭제가 성공적으로 완료되었습니다."),

    // 예약(Reservation)
    RESERVATION_CREATE_SUCCESS(HttpStatus.CREATED, "좌석 예약 생성이 성공적으로 완료되었습니다."),
    RESERVATION_CANCEL_SUCCESS(HttpStatus.OK, "좌석 예약 취소가 성공적으로 완료되었습니다.");

    private final HttpStatus status;
    private final String     message;

}
