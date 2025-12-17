package com.example.demo.domain.performance.model;

import static com.example.demo.common.response.ErrorCode.SEAT_ALREADY_RESERVED;
import static com.example.demo.common.response.ErrorCode.SEAT_ALREADY_SOLD;
import static com.example.demo.common.response.ErrorCode.SEAT_NOT_AVAILABLE;
import static com.example.demo.domain.performance.model.SeatStatus.AVAILABLE;
import static com.example.demo.domain.performance.model.SeatStatus.RESERVED;
import static com.example.demo.domain.performance.model.SeatStatus.SOLD;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import com.example.demo.common.error.BusinessException;
import com.example.demo.common.model.BaseAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PackageName : com.example.demo.domain.performance.model
 * FileName    : Seat
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 공연 좌석 정보 엔티티
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Seat extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "seat_id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    @Setter
    @NotBlank
    private String seatCode;            // 좌석 번호 (예: A-1, B-12)

    @Column(nullable = false)
    private int price;                  // 가격

    @Column(nullable = false)
    @Enumerated(STRING)
    @Setter
    @NotNull
    private SeatStatus status;          // 좌석 상태: AVAILABLE(가능), RESERVED(예약됨), SOLD(판매됨)

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "performance_id",
                nullable = false,
                updatable = false,
                foreignKey = @ForeignKey(name = "FK_seats_performances"))
    @NotNull
    private Performance performance;    // 공연

    @Version
    @Getter(PRIVATE)
    private Long version;

    private Seat(final String seatCode, final int price, final SeatStatus status) {
        this.seatCode = seatCode;
        this.price = price;
        this.status = status;
    }

    // ========================= 생성자 메서드 =========================

    /**
     * Seat 객체 생성
     *
     * @param seatCode    - 좌석 번호
     * @param price       - 가격
     * @param performance - Performance 객체
     * @return Seat 객체
     */
    public static Seat of(final String seatCode,
                          final int price,
                          final Performance performance) {
        validatePrice(price);
        return of(seatCode, price, AVAILABLE, performance);
    }

    /**
     * Seat 객체 생성
     *
     * @param seatCode    - 좌석 번호
     * @param price       - 가격
     * @param status      - 좌석 상태
     * @param performance - Performance 객체
     * @return Seat 객체
     */
    public static Seat of(final String seatCode,
                          final int price,
                          final SeatStatus status,
                          final Performance performance) {
        validatePrice(price);
        Seat seat = new Seat(seatCode, price, status);
        seat.setRelationshipWithPerformance(performance);
        return seat;
    }

    // ========================= 검증 메서드 =========================

    /**
     * 가격 유효성을 검사합니다.
     *
     * @param input - 입력값
     */
    private static void validatePrice(final int input) {
        if (input < 0) throw new IllegalArgumentException("가격은 0원 이상이어야 합니다");
    }

    // ========================= 연관관계 메서드 =========================

    /**
     * 공연과의 관계를 설정합니다.
     *
     * @param performance - 공연
     */
    private void setRelationshipWithPerformance(final Performance performance) {
        this.performance = performance;
        performance.getSeats().add(this);
    }

    // ========================= 비즈니스 메서드 =========================

    /**
     * 가격을 변경합니다.
     *
     * @param input - 입력값
     */
    public void setPrice(final int input) {
        validatePrice(input);
        price = input;
    }

    /**
     * 좌석을 예약 처리합니다.
     */
    public void reserve() {
        if (status != AVAILABLE) throw new BusinessException(SEAT_ALREADY_RESERVED);
        status = RESERVED;
    }

    /**
     * 예약을 취소합니다.
     */
    public void cancel() {
        if (status == SOLD) throw new BusinessException(SEAT_ALREADY_SOLD);
        status = AVAILABLE;
    }

    /**
     * 결제 완료 처리합니다.
     */
    public void confirmSale() {
        if (status != RESERVED) throw new BusinessException(SEAT_NOT_AVAILABLE);
        status = SOLD;
    }

}
