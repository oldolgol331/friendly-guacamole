package com.example.demo.domain.reservation.model;

import static com.example.demo.common.response.ErrorCode.INVALID_CONFIRMED_AT;
import static com.example.demo.common.response.ErrorCode.INVALID_EXPIRED_AT;
import static com.example.demo.common.response.ErrorCode.INVALID_RESERVATION_STATUS;
import static com.example.demo.domain.reservation.model.ReservationStatus.CANCELLED;
import static com.example.demo.domain.reservation.model.ReservationStatus.CONFIRMED;
import static com.example.demo.domain.reservation.model.ReservationStatus.PENDING_PAYMENT;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import com.example.demo.common.error.BusinessException;
import com.example.demo.common.model.BaseAuditingEntity;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.performance.model.Seat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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

    @Enumerated(STRING)
    @Column(nullable = false)
    @NotNull
    private ReservationStatus status;       // 예약 상태

    @Column(nullable = false)
    @NotNull
    private LocalDateTime expiredAt;        // 임시 점유 만료 시간

    private LocalDateTime confirmedAt;      // 예약 확정 시간

    @OneToOne(mappedBy = "reservation")
    private Payment payment;                // 결제 정보

    private Reservation(final Seat seat, final LocalDateTime expiredAt) {
        this.seat = seat;
        this.status = PENDING_PAYMENT;
        this.expiredAt = expiredAt;
    }

    // ========================= 생성자 메서드 =========================

    /**
     * Reservation 객체 생성
     *
     * @param account   - Account 객체
     * @param seat      - Seat 객체
     * @param expiredAt - 예약 임시 점유 만료 시간
     * @return Reservation 객체
     */
    public static Reservation of(final Account account, final Seat seat, final LocalDateTime expiredAt) {
        validateExpiredAt(expiredAt);
        Reservation reservation = new Reservation(seat, expiredAt);
        reservation.setRelationshipWithAccount(account);
        return reservation;
    }

    // ========================= 검증 메서드 =========================

    /**
     * 예약 임시 점유 만료 시간을 검증합니다.
     *
     * @param input - 예약 임시 점유 만료 시간
     */
    private static void validateExpiredAt(final LocalDateTime input) {
        if (input == null || input.isBefore(LocalDateTime.now())) throw new BusinessException(INVALID_EXPIRED_AT);
    }

    /**
     * 예약 확정 시간을 검증합니다.
     *
     * @param input - 예약 확정 시간
     */
    private static void validateConfirmedAt(final LocalDateTime input) {
        if (input == null) throw new BusinessException(INVALID_CONFIRMED_AT);
    }

    // ========================= 연관관계 메서드 =========================

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
     * 예약 ID(복합키)를 반환합니다.
     *
     * @return ReservationId 객체
     */
    public ReservationId getReservationId() {
        return new ReservationId(accountId, seatId);
    }

    /**
     * 예약을 확정합니다.
     *
     * @param confirmedAt - 예약 확정 시간
     */
    public void confirm(final LocalDateTime confirmedAt) {
        if (status != PENDING_PAYMENT) throw new BusinessException(INVALID_RESERVATION_STATUS);
        validateConfirmedAt(confirmedAt);
        status = CONFIRMED;
        this.confirmedAt = confirmedAt;
    }

    /**
     * 예약을 취소합니다.
     */
    public void cancel() {
        if (status == CANCELLED) throw new BusinessException(INVALID_RESERVATION_STATUS);
        status = CANCELLED;
        seat.cancel();
    }

}
