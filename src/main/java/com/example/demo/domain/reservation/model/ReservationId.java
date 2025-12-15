package com.example.demo.domain.reservation.model;

import static lombok.AccessLevel.PRIVATE;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.reservation.model
 * FileName    : ReservationId
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 예약 정보(Reservation) 엔티티 복합키
 *               계정 정보(Account) + 좌석 정보(Seat)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Getter
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode
public final class ReservationId {
    private UUID accountId;
    private Long seatId;
}
