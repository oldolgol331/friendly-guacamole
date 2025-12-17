package com.example.demo.domain.reservation.model;

import static com.example.demo.common.response.ErrorCode.INVALID_RESERVATION_TIME;
import static com.example.demo.domain.performance.model.SeatStatus.RESERVED;
import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import com.example.demo.common.error.BusinessException;
import com.example.demo.common.model.BaseAuditingEntity;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.performance.model.Seat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.reservation.model
 * FileName    : Reservation
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 좌석 예약 정보 엔티티
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Entity
@Table(name = "reservations")
@IdClass(ReservationId.class)
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Reservation extends BaseAuditingEntity {

    @Id
    @Column(name = "account_id",
            columnDefinition = "BINARY(16)",
            nullable = false,
            insertable = false,
            updatable = false)
    private UUID accountId;

    @Id
    @Column(name = "seat_id", nullable = false, insertable = false, updatable = false)
    private Long seatId;

    @MapsId("accountId")
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id",
                columnDefinition = "BINARY(16)",
                nullable = false,
                insertable = false,
                updatable = false,
                foreignKey = @ForeignKey(name = "FK_reservations_accounts"))
    private Account account;

    @MapsId("seatId")
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "seat_id",
                nullable = false,
                insertable = false,
                updatable = false,
                foreignKey = @ForeignKey(name = "FK_reservations_seats"))
    private Seat seat;

    private LocalDateTime reservationTime;  // 예약 확정 시간

    private Reservation(final Seat seat) {
        this.seat = seat;
    }

    // ========================= 생성자 메서드 =========================

    /**
     * Reservation 객체 생성
     *
     * @param account - Account 객체
     * @param seat    - Seat 객체
     * @return Reservation 객체
     */
    public static Reservation of(final Account account, final Seat seat) {
        Reservation reservation = new Reservation(seat);
        seat.setStatus(RESERVED);
        reservation.setRelationshipWithAccount(account);
        return reservation;
    }

    // ========================= 연관관계 메서드 =========================

    /**
     * 예약 확정 시간을 검증합니다.
     *
     * @param input - 예약 확정 시간
     */
    private static void validateReservationTime(final LocalDateTime input) {
        if (input == null || input.isBefore(LocalDateTime.now())) throw new BusinessException(INVALID_RESERVATION_TIME);
    }

    // ========================= 검증 메서드 =========================

    /**
     * 계정과의 관계를 설정합니다.
     *
     * @param account - 계정
     */
    private void setRelationshipWithAccount(final Account account) {
        this.account = account;
        account.getReservations().add(this);
    }

    // ========================= 비즈니스 메서드 =========================

    /**
     * 예약을 확정합니다.
     *
     * @param reservationTime - 예약 확정 시간
     */
    public void complete(final LocalDateTime reservationTime) {
        validateReservationTime(reservationTime);
        this.reservationTime = reservationTime;
    }

    /**
     * 예약을 취소합니다.
     */
    public void cancel() {
        seat.cancel();
    }

}
