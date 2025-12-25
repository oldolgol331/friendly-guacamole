package com.example.demo.domain.reservation.model;

/**
 * PackageName : com.example.demo.domain.reservation.model
 * FileName    : ReservationStatus
 * Author      : oldolgol331
 * Date        : 25. 12. 25.
 * Description : 예약 상태
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 25.   oldolgol331          Initial creation
 */
public enum ReservationStatus {
    PENDING_PAYMENT,    // 결제 대기 (임시 점유)
    CONFIRMED,          // 예약 확정
    CANCELLED           // 취소됨
}
