package com.example.demo.domain.reservation.facade;

import static com.example.demo.common.util.DateUtils.convertUnixToLocalDateTime;

import com.example.demo.common.error.BusinessException;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.service.AccountService;
import com.example.demo.domain.performance.model.Performance;
import com.example.demo.domain.performance.model.Seat;
import com.example.demo.domain.reservation.dto.PaymentRequest.PaymentCancelRequest;
import com.example.demo.domain.reservation.dto.PaymentRequest.PrePaymentRequest;
import com.example.demo.domain.reservation.dto.PaymentResponse.PrePaymentInfoResponse;
import com.example.demo.domain.reservation.dto.PaymentVerifyCommand;
import com.example.demo.domain.reservation.model.Payment;
import com.example.demo.domain.reservation.model.Reservation;
import com.example.demo.domain.reservation.service.PaymentService;
import com.example.demo.domain.reservation.service.ReservationService;
import com.example.demo.infra.payment.portone.client.PortOnePGPaymentApiClient;
import com.example.demo.infra.payment.portone.dto.PortOneCancelPaymentApiRequest;
import com.example.demo.infra.payment.portone.dto.PortOnePaymentApiRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.example.demo.domain.reservation.facade
 * FileName    : ReservationFacade
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : 좌석 예약, 결제 로직 수행
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationFacade {

    private final ReservationService reservationService;
    private final PaymentService     paymentService;
    private final AccountService     accountService;

    private final PortOnePGPaymentApiClient portOneApiClient;

    /**
     * 결제 검증 실패 사유를 반환합니다.
     *
     * @param e - 결제 검증 실패 예외
     * @return 결제 검증 실패 사유
     */
    private static String getVerifyFailReason(final BusinessException e) {
        String reason = "검증 실패: 알 수 없는 이유";
        switch (e.getErrorCode()) {
            case EXPIRE_PAYMENT_VERIFICATION_TIME -> reason = "검증 실패: 검증 유효시간 초과";
            case PAYMENT_VERIFICATION_FAILED -> reason = "검증 실패: PG사 결제 정보와 서버 결제 정보 불일치";
            case PAYMENT_AMOUNT_MISMATCH -> reason = "검증 실패: 금액 불일치";
            case PAYMENT_ACCOUNT_MISMATCH -> reason = "검증 실패: 결제 시도 계정 불일치";
            case PAYMENT_NOT_FOUND -> reason = "검증 실패: 존재하지 않는 결제 정보";
            case PAYMENT_NOT_COMPLETED -> reason = "검증 실패: PG사 결제가 미완료 상태";
        }
        return reason;
    }

    /**
     * 결제 전 서버에 결제 정보를 저장해놓고, PG사 결제 ID를 발급합니다.
     *
     * @param accountId - 계정 ID
     * @param request   - 결제 전 사전 정보 요청 DTO
     * @return 사전 결제 정보 응답 DTO
     */
    @Transactional
    public PrePaymentInfoResponse savePrePayment(final UUID accountId,
                                                 final PrePaymentRequest request,
                                                 final String clientIp) {
        Account     account     = accountService.findByAccountId(accountId);
        Reservation reservation = reservationService.findReservationById(accountId, request.getSeatId());
        Seat        seat        = reservation.getSeat();
        Performance performance = seat.getPerformance();

        String paymentInfo = "%s %s".formatted(performance.getName(), seat.getSeatCode());

        return paymentService.savePrePayment(account, reservation, request, paymentInfo, clientIp);
    }

    // ========================= 내부 메서드 =========================

    /**
     * 결제 검증 및 최종 승인을 합니다. 클라이언트가 결제 성공 후 호출하는 API의 진입점 역할입니다.
     *
     * @param accountId  - 계정 ID
     * @param paymentKey - PG사 결제 ID
     * @param clientIp   - 클라이언트 IP
     */
    public void verifyPayment(final UUID accountId, final String paymentKey, final String clientIp) {
        accountService.findByAccountId(accountId);

        var pgRequest  = new PortOnePaymentApiRequest(paymentKey);
        var pgResponse = portOneApiClient.getPayment(pgRequest);

        var command = new PaymentVerifyCommand(pgResponse.getId(),
                                               pgResponse.getAmount().getTotal(),
                                               pgResponse.getStatus(),
                                               pgResponse.getMethod(),
                                               convertUnixToLocalDateTime(pgResponse.getPaidAt()),
                                               pgResponse.getReceiptUrl());

        try {
            paymentService.verifyAndApprove(accountId, command, clientIp);
        } catch (BusinessException e) {
            log.error("결제 검증 실패로 인한 자동 취소 진행 - paymentKey: {}", paymentKey);

            String cancelReason = getVerifyFailReason(e);
            try {
                portOneApiClient.cancelPayment(paymentKey,
                                               new PortOneCancelPaymentApiRequest(paymentKey,
                                                                                  pgResponse.getAmount()
                                                                                            .getTotal()
                                                                                            .intValue(),
                                                                                  cancelReason));
                paymentService.cancelPayment(paymentKey, cancelReason);
                log.info("결제 검증 실패로 인한 포트원 결제 및 서버 결제 취소 완료 - paymentKey: {}", paymentKey);
            } catch (Exception portOneCancelException) {
                log.error("포트원 결제 취소 실패 - 포트원 수동 취소 필요, paymentKey: {}", paymentKey, portOneCancelException);
                alertManualPaymentCancelRequired(paymentKey, cancelReason);
            }

            throw e;
        }
    }

    /**
     * 결제 취소 요청을 처리합니다.
     *
     * @param accountId - 계정 ID
     * @param request   - 결제 취소 요청 DTO
     */
    @Transactional
    public void refundPayment(final UUID accountId, final PaymentCancelRequest request) {
        accountService.findByAccountId(accountId);
        Payment payment = paymentService.findByAccountIdAndPaymentKey(accountId, request.getPaymentId());

        portOneApiClient.getPayment(new PortOnePaymentApiRequest(request.getPaymentId()));

        paymentService.refundPayment(payment, request.getRefundReason());
        reservationService.cancelReservation(payment.getReservation());
    }

    // ========================= 내부 메서드 =========================

    /**
     * 포트원 결제 취소 실패 시 관리자에게 알림을 보냅니다.
     *
     * @param paymentKey - PG사 결제 ID
     * @param reason     - 결제 취소 실패 사유
     */
    private void alertManualPaymentCancelRequired(final String paymentKey, final String reason) {
        log.error("포트원 수동 취소 필요 - paymentKey: {}, reason: {}", paymentKey, reason);
        // TODO: 관리자 알림 또는 모니터링 시스템에 알림 전송 로직 추가
    }

}
