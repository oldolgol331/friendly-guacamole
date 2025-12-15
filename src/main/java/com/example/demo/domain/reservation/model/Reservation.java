package com.example.demo.domain.reservation.model;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import com.example.demo.common.model.BaseAuditingEntity;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.seat.model.Seat;
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

    @Column(nullable = false)
    private LocalDateTime reservationTime;  // 예약 확정 시간

    private Reservation(final Account account, final Seat seat, final LocalDateTime reservationTime) {
        this.account = account;
        this.seat = seat;
        this.reservationTime = reservationTime;
    }

    // ========================= 생성자 메서드 =========================

    /**
     * Reservation 객체 생성
     *
     * @param account - Account 객체
     * @param seat - Seat 객체
     * @param reservationTime - 예약 확정 시간
     * @return Reservation 객체
     */
    public static Reservation of(final Account account, final Seat seat, final LocalDateTime reservationTime) {
        if (reservationTime.isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("예약 확정 시간은 현재 시간보다 과거일 수 없습니다.");
        return new Reservation(account, seat, reservationTime);
    }

}
