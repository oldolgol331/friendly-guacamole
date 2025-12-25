package com.example.demo.domain.reservation.listener;

import com.example.demo.domain.reservation.event.PaymentCanceledEvent;
import com.example.demo.domain.reservation.event.PaymentCompletedEvent;
import com.example.demo.domain.reservation.model.Payment;
import com.example.demo.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * PackageName : com.example.demo.domain.reservation.listener
 * FileName    : PaymentEventListener
 * Author      : oldolgol331
 * Date        : 25. 12. 21.
 * Description : 결제 이벤트 리스너
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 21.   oldolgol331          Initial creation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final ReservationService reservationService;

    /**
     * 결제 완료 이벤트를 처리합니다.
     * - 예약 확정
     * - 좌석 상태 변경
     *
     * @param event - 결제 완료 이벤트
     */
    @Async
    @TransactionalEventListener
    public void handlePaymentCompleted(final PaymentCompletedEvent event) {
        Payment payment = event.getPayment();
        try {
            payment.getReservation().confirm(event.getOccurredAt());
            payment.getReservation().getSeat().confirmSale();

            log.info("결제 완료 이벤트 처리 완료 - paymentId: {}, reservationId: {}",
                     payment.getId(),
                     payment.getReservation().getReservationId());
        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 중 오류 발생 - paymentId: {}", payment.getId(), e);
            handlePaymentEventFailure(payment.getPaymentKey(), e);
        }
    }

    /**
     * 결제 취소 이벤트를 처리합니다.
     * - 예약 취소
     *
     * @param event - 결제 취소 이벤트
     */
    @TransactionalEventListener
    public void handlePaymentCanceled(final PaymentCanceledEvent event) {
        Payment payment = event.getPayment();
        try {
            reservationService.cancelReservation(payment.getReservation());

            log.info("결제 취소 이벤트 처리 완료 - paymentId: {}, reservationId: {}",
                     payment.getId(),
                     payment.getReservation().getReservationId());
        } catch (Exception e) {
            log.error("결제 취소 이벤트 처리 중 오류 발생 - paymentId: {}", payment.getId(), e);
            throw e;
        }
    }

    // ========================= 내부 메서드 =========================

    private void handlePaymentEventFailure(final String paymentId, final Exception e) {
        log.error("결제 완료 이벤트 처리 실패 - 수동 확인 필요, paymentId: {}", paymentId);
        // TODO: 실패한 이벤트를 별도 큐에 저장하여 재처리하거나 관리자 알림 전송
    }

}
