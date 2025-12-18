package com.example.demo.domain.reservation.model;

/**
 * PackageName : com.example.demo.domain.reservation.model
 * FileName    : PaymentStatus
 * Author      : oldolgol331
 * Date        : 25. 12. 18.
 * Description : 결제 상태
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 18.   oldolgol331          Initial creation
 */
public enum PaymentStatus {
    PENDING,    // 결제 보류
    FAILED,     // 결제 실패
    PAID,       // 결제 완료
    CANCELLED   // 결제 취소
}
