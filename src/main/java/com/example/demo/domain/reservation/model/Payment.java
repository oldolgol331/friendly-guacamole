package com.example.demo.domain.reservation.model;

import static com.example.demo.common.response.ErrorCode.INVALID_PAYMENT_AMOUNT;
import static com.example.demo.common.response.ErrorCode.INVALID_PAYMENT_APPROVAL_TIME;
import static com.example.demo.common.response.ErrorCode.INVALID_PAYMENT_STATUS;
import static com.example.demo.common.response.ErrorCode.PAYMENT_ALREADY_CANCELED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED;
import static com.example.demo.domain.reservation.model.PaymentStatus.CANCELLED;
import static com.example.demo.domain.reservation.model.PaymentStatus.PAID;
import static com.example.demo.domain.reservation.model.PaymentStatus.PENDING;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
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
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.reservation.model
 * FileName    : Payment
 * Author      : oldolgol331
 * Date        : 25. 12. 18.
 * Description : 결제 정보 엔티티
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 18.   oldolgol331          Initial creation
 */
@Entity
@Table(name = "payments",
       uniqueConstraints = @UniqueConstraint(name = "UK_payments_payment_key", columnNames = "payment_key"))
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Payment extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private Long id;                    // ID

    @ManyToOne(fetch = LAZY)
    @JoinColumns({
            @JoinColumn(name = "account_id",
                        referencedColumnName = "account_id",
                        insertable = false,
                        foreignKey = @ForeignKey(name = "FK_payments_reservations")),
            @JoinColumn(name = "seat_id",
                        referencedColumnName = "seat_id",
                        insertable = false,
                        foreignKey = @ForeignKey(name = "FK_payments_reservations"))
    })
    private Reservation reservation;    // 예약 정보


    @Column(nullable = false, updatable = false)
    @NotBlank
    private String paymentKey;          // PG사 결제 ID

    @Column(nullable = false)
    @NotBlank
    private String paymentMethod;       // 결제 방법

    @Column(nullable = false)
    @NotBlank
    private String paymentInfo;         // 결제 상품 정보: {공연명} {좌석 번호}

    @Column(nullable = false, precision = 19, scale = 4)
    @NotNull
    private BigDecimal amount;          // 결제 금액

    @Enumerated(STRING)
    @Column(nullable = false)
    @NotNull
    private PaymentStatus status;       // 결제 상태

    @Column(nullable = false, updatable = false)
    private String clientIp;            // 결제 요청 클라이언트 IP

    private LocalDateTime approvedAt;   // 결제 승인 일시

    private String receiptUrl;          // 영수증 URL

    private LocalDateTime canceledAt;   // 결제 취소 일시

    private String cancelReason;        // 결제 취소 사유

    private Payment(final Reservation reservation,
                    final String paymentKey,
                    final String paymentMethod,
                    final String paymentInfo,
                    final BigDecimal amount,
                    final String clientIp) {
        this.reservation = reservation;
        this.paymentKey = paymentKey;
        this.paymentMethod = paymentMethod;
        this.paymentInfo = paymentInfo;
        this.amount = amount;
        this.status = PENDING;
        this.clientIp = clientIp;
    }

    // ========================= 생성자 메서드 =========================

    /**
     * Payment 객체 생성
     *
     * @param reservation - Reservation 객체
     * @param paymentKey  - PG사 결제 ID
     * @param amount      - 결제 금액
     * @param clientIp    - 결제 요청 클라이언트 IP
     * @return Payment 객체
     */
    public static Payment of(final Reservation reservation,
                             final String paymentKey,
                             final String paymentInfo,
                             final BigDecimal amount,
                             final String clientIp) {
        validateAmount(amount);
        return new Payment(reservation, paymentKey, "UNKNOWN", paymentInfo, amount, clientIp);
    }

    /**
     * Payment 객체 생성
     *
     * @param reservation   - Reservation 객체
     * @param paymentKey    - PG사 결제 ID
     * @param paymentMethod - 결제 방법
     * @param amount        - 결제 금액
     * @param clientIp      - 결제 요청 클라이언트 IP
     * @return Payment 객체
     */
    public static Payment of(final Reservation reservation,
                             final String paymentKey,
                             final String paymentMethod,
                             final String paymentInfo,
                             final BigDecimal amount,
                             final String clientIp) {
        validateAmount(amount);
        return new Payment(reservation, paymentKey, paymentMethod, paymentInfo, amount, clientIp);
    }

    // ========================= 검증 메서드 =========================

    /**
     * 결제 금액을 검증합니다.
     *
     * @param input - 입력값
     */
    private static void validateAmount(final BigDecimal input) {
        if (input == null || input.compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException(INVALID_PAYMENT_AMOUNT);
    }

    // ========================= JPA 콜백 메서드 =========================

    /**
     * 결제 상태를 확인하고, 결제 상태가 null인 경우 PENDING으로 설정합니다.
     */
    @PrePersist
    private void statusCheck() {
        if (status == null) status = PENDING;
    }

    // ========================= 비즈니스 메서드 =========================

    /**
     * 결제 승인 처리합니다.
     *
     * @param paymentMethod - 결제 방법
     * @param approvedAt    - 결제 승인 시간
     */
    public void approve(final String paymentMethod, final LocalDateTime approvedAt, final String receiptUrl) {
        if (paymentMethod == null || paymentMethod.isBlank()) throw new BusinessException(PAYMENT_METHOD_NOT_SUPPORTED);
        if (approvedAt == null) throw new BusinessException(INVALID_PAYMENT_APPROVAL_TIME);
        if (status != PENDING) throw new BusinessException(INVALID_PAYMENT_STATUS);

        this.paymentMethod = paymentMethod;
        status = PAID;
        this.approvedAt = approvedAt;
        this.receiptUrl = receiptUrl;
    }

    /**
     * 결제를 취소합니다.
     *
     * @param cancelReason - 결제 취소 사유
     */
    public void cancel(final String cancelReason) {
        switch (status) {
            case CANCELLED -> throw new BusinessException(PAYMENT_ALREADY_CANCELED);
            case FAILED, PENDING -> throw new BusinessException(INVALID_PAYMENT_STATUS);
        }
        status = CANCELLED;
        this.cancelReason = cancelReason;
        canceledAt = LocalDateTime.now();
    }

}
