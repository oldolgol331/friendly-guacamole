package com.example.demo.domain.reservation.facade;

import static com.example.demo.common.response.ErrorCode.EXPIRE_PAYMENT_VERIFICATION_TIME;
import static com.example.demo.common.response.ErrorCode.PAYMENT_ACCOUNT_MISMATCH;
import static com.example.demo.common.response.ErrorCode.PAYMENT_ALREADY_CANCELED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_AMOUNT_MISMATCH;
import static com.example.demo.common.response.ErrorCode.PAYMENT_NOT_COMPLETED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_NOT_FOUND;
import static com.example.demo.common.response.ErrorCode.PAYMENT_VERIFICATION_FAILED;
import static com.example.demo.common.util.TestUtils.createAccount;
import static com.example.demo.common.util.TestUtils.createPayment;
import static com.example.demo.common.util.TestUtils.createPerformance;
import static com.example.demo.common.util.TestUtils.createReservation;
import static com.example.demo.common.util.TestUtils.createSeat;
import static com.example.demo.common.util.TestUtils.generateIpAddress;
import static com.example.demo.common.util.TestUtils.generatePaymentKey;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.common.error.BusinessException;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.service.AccountService;
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
import com.example.demo.infra.payment.portone.dto.PortOnePaymentApiResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * PackageName : com.example.demo.domain.reservation.facade
 * FileName    : ReservationFacadeTest
 * Author      : oldolgol331
 * Date        : 25. 12. 22.
 * Description : ReservationFacade 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 22.   oldolgol331          Initial creation
 */
@ExtendWith(MockitoExtension.class)
class ReservationFacadeTest {

    @InjectMocks
    ReservationFacade         reservationFacade;
    @Mock
    ReservationService        reservationService;
    @Mock
    PaymentService            paymentService;
    @Mock
    AccountService            accountService;
    @Mock
    PortOnePGPaymentApiClient portOneApiClient;

    @Nested
    @DisplayName("savePrePayment() 테스트")
    class SavePrePaymentTests {

        @RepeatedTest(10)
        @DisplayName("사전 결제 정보 저장")
        void savePrePayment() {
            // given
            UUID              accountId = UUID.randomUUID();
            PrePaymentRequest request   = new PrePaymentRequest(1L, 1L, "CARD", 10000);
            String            clientIp  = "192.168.0.1";

            Account     account     = createAccount();
            Reservation reservation = createReservation(account, createSeat(createPerformance()));
            PrePaymentInfoResponse response = new PrePaymentInfoResponse(
                    "Payment:20251222131234:abc123def456ghi789",
                    1L,
                    1L,
                    "Test Performance A1",
                    10000,
                    LocalDateTime.now().plusMinutes(30)
            );

            when(accountService.findByAccountId(eq(accountId))).thenReturn(account);
            when(reservationService.findReservationById(eq(accountId), eq(request.getSeatId()))).thenReturn(
                    reservation);
            when(paymentService.savePrePayment(eq(account), eq(reservation), eq(request), anyString(), eq(clientIp)))
                    .thenReturn(response);

            // when
            PrePaymentInfoResponse result = reservationFacade.savePrePayment(accountId, request, clientIp);

            // then
            assertEquals(response, result);

            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(reservationService, times(1)).findReservationById(eq(accountId), eq(request.getSeatId()));
            verify(paymentService, times(1)).savePrePayment(eq(account), eq(reservation), eq(request), anyString(),
                                                            eq(clientIp));
        }

    }

    @Nested
    @DisplayName("verifyPayment() 테스트")
    class VerifyPaymentTests {

        @RepeatedTest(10)
        @DisplayName("결제 검증 및 승인")
        void verifyPayment() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();

            Account account = createAccount();
            PortOnePaymentApiResponse portOneResponse = new PortOnePaymentApiResponse(
                    paymentKey,
                    "PAID",
                    new PortOnePaymentApiResponse.Amount(BigDecimal.valueOf(10000)),
                    "CARD",
                    "https://receipt.url",
                    "1735689600",
                    "1735689600"
            );

            PaymentVerifyCommand command = new PaymentVerifyCommand(
                    paymentKey,
                    BigDecimal.valueOf(1000),
                    "PAID",
                    "CARD",
                    LocalDateTime.of(2025, 12, 22, 13, 12, 34),
                    "https://receipt.url"
            );

            when(accountService.findByAccountId(eq(accountId))).thenReturn(account);
            when(portOneApiClient.getPayment(any(PortOnePaymentApiRequest.class))).thenReturn(portOneResponse);

            // when
            reservationFacade.verifyPayment(accountId, paymentKey, clientIp);

            // then
            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(portOneApiClient, times(1)).getPayment(any(PortOnePaymentApiRequest.class));
            verify(paymentService, times(1))
                    .verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class), eq(clientIp));
            verify(portOneApiClient, never()).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            verify(paymentService, never()).cancelPayment(anyString(), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 실패 - 검증 시간 초과")
        void verifyPayment_expiredVerificationTime() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();

            Account account = createAccount();
            PortOnePaymentApiResponse portOneResponse = new PortOnePaymentApiResponse(

                    paymentKey,
                    "PAID",
                    new PortOnePaymentApiResponse.Amount(BigDecimal.valueOf(10000)),
                    "CARD",
                    "https://receipt.url",
                    "1735689600",
                    "1735689600"
            );

            when(accountService.findByAccountId(eq(accountId))).thenReturn(account);
            when(portOneApiClient.getPayment(any(PortOnePaymentApiRequest.class))).thenReturn(portOneResponse);
            doThrow(new BusinessException(EXPIRE_PAYMENT_VERIFICATION_TIME))
                    .when(paymentService)
                    .verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class), eq(clientIp));
            doNothing().when(portOneApiClient).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            doNothing().when(paymentService).cancelPayment(anyString(), anyString());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationFacade.verifyPayment(accountId, paymentKey,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(EXPIRE_PAYMENT_VERIFICATION_TIME, exception.getErrorCode(),
                                         "errorCode는 EXPIRE_PAYMENT_VERIFICATION_TIME이어야 합니다."));

            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(portOneApiClient, times(1)).getPayment(any(PortOnePaymentApiRequest.class));
            verify(paymentService, times(1)).verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class),
                                                              eq(clientIp));
            verify(portOneApiClient, times(1)).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            verify(paymentService, times(1)).cancelPayment(anyString(), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 실패 - 결제 정보 불일치")
        void verifyPayment_paymentVerificationFailed() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();

            Account account = createAccount();
            PortOnePaymentApiResponse portOneResponse = new PortOnePaymentApiResponse(
                    paymentKey,
                    "PAID",
                    new PortOnePaymentApiResponse.Amount(BigDecimal.valueOf(10000)),
                    "CARD",
                    "https://receipt.url",
                    "1735689600",
                    "1735689600"
            );

            when(accountService.findByAccountId(eq(accountId))).thenReturn(account);
            when(portOneApiClient.getPayment(any(PortOnePaymentApiRequest.class))).thenReturn(portOneResponse);
            doThrow(new BusinessException(PAYMENT_VERIFICATION_FAILED))
                    .when(paymentService)
                    .verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class), eq(clientIp));
            doNothing().when(portOneApiClient).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            doNothing().when(paymentService).cancelPayment(anyString(), anyString());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationFacade.verifyPayment(accountId, paymentKey,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_VERIFICATION_FAILED, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_VERIFICATION_FAILED이어야 합니다."));

            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(portOneApiClient, times(1)).getPayment(any(PortOnePaymentApiRequest.class));
            verify(paymentService, times(1)).verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class),
                                                              eq(clientIp));
            verify(portOneApiClient, times(1)).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            verify(paymentService, times(1)).cancelPayment(anyString(), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 실패 - 금액 불일치")
        void verifyPayment_amountMismatch() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();

            Account account = createAccount();
            PortOnePaymentApiResponse portOneResponse = new PortOnePaymentApiResponse(
                    paymentKey,
                    "PAID",
                    new PortOnePaymentApiResponse.Amount(BigDecimal.valueOf(10000)),
                    "CARD",
                    "https://receipt.url",
                    "1735689600",
                    "1735689600"
            );

            when(accountService.findByAccountId(eq(accountId))).thenReturn(account);
            when(portOneApiClient.getPayment(any(PortOnePaymentApiRequest.class))).thenReturn(portOneResponse);
            doThrow(new BusinessException(PAYMENT_AMOUNT_MISMATCH))
                    .when(paymentService)
                    .verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class), eq(clientIp));
            doNothing().when(portOneApiClient).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            doNothing().when(paymentService).cancelPayment(anyString(), anyString());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationFacade.verifyPayment(accountId, paymentKey,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_AMOUNT_MISMATCH, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_AMOUNT_MISMATCH이어야 합니다."));

            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(portOneApiClient, times(1)).getPayment(any(PortOnePaymentApiRequest.class));
            verify(paymentService, times(1)).verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class),
                                                              eq(clientIp));
            verify(portOneApiClient, times(1)).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            verify(paymentService, times(1)).cancelPayment(anyString(), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 실패 - 계정 불일치")
        void verifyPayment_accountMismatch() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();

            Account account = createAccount();
            PortOnePaymentApiResponse portOneResponse = new PortOnePaymentApiResponse(
                    paymentKey,
                    "PAID",
                    new PortOnePaymentApiResponse.Amount(BigDecimal.valueOf(10000)),
                    "CARD",
                    "https://receipt.url",
                    "1735689600",
                    "1735689600"
            );

            when(accountService.findByAccountId(eq(accountId))).thenReturn(account);
            when(portOneApiClient.getPayment(any(PortOnePaymentApiRequest.class))).thenReturn(portOneResponse);
            doThrow(new BusinessException(PAYMENT_ACCOUNT_MISMATCH))
                    .when(paymentService)
                    .verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class), eq(clientIp));
            doNothing().when(portOneApiClient).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            doNothing().when(paymentService).cancelPayment(anyString(), anyString());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationFacade.verifyPayment(accountId, paymentKey,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_ACCOUNT_MISMATCH, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_ACCOUNT_MISMATCH이어야 합니다."));

            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(portOneApiClient, times(1)).getPayment(any(PortOnePaymentApiRequest.class));
            verify(paymentService, times(1)).verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class),
                                                              eq(clientIp));
            verify(portOneApiClient, times(1)).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            verify(paymentService, times(1)).cancelPayment(anyString(), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 실패 - 결제 미완료")
        void verifyPayment_notCompleted() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();

            Account account = createAccount();
            PortOnePaymentApiResponse portOneResponse = new PortOnePaymentApiResponse(
                    paymentKey,
                    "PAID",
                    new PortOnePaymentApiResponse.Amount(BigDecimal.valueOf(10000)),
                    "CARD",
                    "https://receipt.url",
                    "1735689600",
                    "1735689600"
            );

            when(accountService.findByAccountId(eq(accountId))).thenReturn(account);
            when(portOneApiClient.getPayment(any(PortOnePaymentApiRequest.class))).thenReturn(portOneResponse);
            doThrow(new BusinessException(PAYMENT_NOT_COMPLETED))
                    .when(paymentService)
                    .verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class), eq(clientIp));
            doNothing().when(portOneApiClient).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            doNothing().when(paymentService).cancelPayment(anyString(), anyString());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationFacade.verifyPayment(accountId, paymentKey,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_NOT_COMPLETED, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_NOT_COMPLETED이어야 합니다."));

            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(portOneApiClient, times(1)).getPayment(any(PortOnePaymentApiRequest.class));
            verify(paymentService, times(1)).verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class),
                                                              eq(clientIp));
            verify(portOneApiClient, times(1)).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            verify(paymentService, times(1)).cancelPayment(anyString(), anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 실패 - 결제 없음")
        void verifyPayment_notFound() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();

            Account account = createAccount();
            PortOnePaymentApiResponse portOneResponse = new PortOnePaymentApiResponse(
                    paymentKey,
                    "PAID",
                    new PortOnePaymentApiResponse.Amount(BigDecimal.valueOf(10000)),
                    "CARD",
                    "https://receipt.url",
                    "1735689600",
                    "1735689600"
            );

            when(accountService.findByAccountId(eq(accountId))).thenReturn(account);
            when(portOneApiClient.getPayment(any(PortOnePaymentApiRequest.class))).thenReturn(portOneResponse);
            doThrow(new BusinessException(PAYMENT_NOT_FOUND))
                    .when(paymentService)
                    .verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class), eq(clientIp));
            doNothing().when(portOneApiClient).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            doNothing().when(paymentService).cancelPayment(anyString(), anyString());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationFacade.verifyPayment(accountId, paymentKey,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_NOT_FOUND이어야 합니다."));

            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(portOneApiClient, times(1)).getPayment(any(PortOnePaymentApiRequest.class));
            verify(paymentService, times(1)).verifyAndApprove(eq(accountId), any(PaymentVerifyCommand.class),
                                                              eq(clientIp));
            verify(portOneApiClient, times(1)).cancelPayment(anyString(), any(PortOneCancelPaymentApiRequest.class));
            verify(paymentService, times(1)).cancelPayment(anyString(), anyString());
        }

    }

    @Nested
    @DisplayName("refundPayment() 테스트")
    class RefundPaymentTests {

        @RepeatedTest(10)
        @DisplayName("결제 환불")
        void refundPayment() {
            // given
            UUID                 accountId = UUID.randomUUID();
            PaymentCancelRequest request   = new PaymentCancelRequest("payment123", "고객 요청");
            Account              account   = createAccount();
            Payment              payment   = createPayment(createReservation(account, createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);

            when(accountService.findByAccountId(eq(accountId))).thenReturn(account);
            when(paymentService.findByAccountIdAndPaymentKey(eq(accountId), eq(request.getPaymentId()))).thenReturn(
                    payment);
            when(portOneApiClient.getPayment(any(PortOnePaymentApiRequest.class))).thenReturn(
                    new PortOnePaymentApiResponse(
                            request.getPaymentId(),
                            "PAID",
                            new PortOnePaymentApiResponse.Amount(BigDecimal.valueOf(10000)),
                            "CARD",
                            "https://receipt.url",
                            "1735689600",
                            "1735689600"
                    ));
            doNothing().when(paymentService).refundPayment(eq(payment), eq(request.getRefundReason()));
            doNothing().when(reservationService).cancelReservation(eq(payment.getReservation()));

            // when
            reservationFacade.refundPayment(accountId, request);

            // then
            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(paymentService, times(1)).findByAccountIdAndPaymentKey(eq(accountId), eq(request.getPaymentId()));
            verify(portOneApiClient, times(1)).getPayment(any(PortOnePaymentApiRequest.class));
            verify(paymentService, times(1)).refundPayment(eq(payment), eq(request.getRefundReason()));
            verify(reservationService, times(1)).cancelReservation(eq(payment.getReservation()));
        }

        @RepeatedTest(10)
        @DisplayName("결제 환불 시도 - 계정이 존재하지 않음")
        void refundPayment_accountNotFound() {
            // given
            UUID                 accountId = UUID.randomUUID();
            PaymentCancelRequest request   = new PaymentCancelRequest("payment123", "고객 요청");

            when(accountService.findByAccountId(eq(accountId))).thenThrow(new BusinessException(PAYMENT_NOT_FOUND));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationFacade.refundPayment(accountId, request),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_NOT_FOUND이어야 합니다."));

            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(paymentService, never()).findByAccountIdAndPaymentKey(any(UUID.class), anyString());
            verify(portOneApiClient, never()).getPayment(any(PortOnePaymentApiRequest.class));
            verify(paymentService, never()).refundPayment(any(Payment.class), anyString());
            verify(reservationService, never()).cancelReservation(any(Reservation.class));
        }

        @RepeatedTest(10)
        @DisplayName("결제 환불 시도 - 결제가 존재하지 않음")
        void refundPayment_paymentNotFound() {
            // given
            UUID                 accountId = UUID.randomUUID();
            PaymentCancelRequest request   = new PaymentCancelRequest("payment123", "고객 요청");
            Account              account   = createAccount();

            when(accountService.findByAccountId(eq(accountId))).thenReturn(account);
            when(paymentService.findByAccountIdAndPaymentKey(eq(accountId), eq(request.getPaymentId()))).thenThrow(
                    new BusinessException(PAYMENT_NOT_FOUND));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationFacade.refundPayment(accountId, request),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_NOT_FOUND이어야 합니다."));

            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(paymentService, times(1)).findByAccountIdAndPaymentKey(eq(accountId), eq(request.getPaymentId()));
            verify(portOneApiClient, never()).getPayment(any(PortOnePaymentApiRequest.class));
            verify(paymentService, never()).refundPayment(any(Payment.class), anyString());
            verify(reservationService, never()).cancelReservation(any(Reservation.class));
        }

        @RepeatedTest(10)
        @DisplayName("결제 환불 시도 - 결제가 이미 취소됨")
        void refundPayment_alreadyCanceled() {
            // given
            UUID                 accountId = UUID.randomUUID();
            PaymentCancelRequest request   = new PaymentCancelRequest("payment123", "고객 요청");
            Account              account   = createAccount();
            Payment              payment   = createPayment(createReservation(account, createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);

            when(accountService.findByAccountId(eq(accountId))).thenReturn(account);
            when(paymentService.findByAccountIdAndPaymentKey(eq(accountId), eq(request.getPaymentId()))).thenReturn(
                    payment);
            when(portOneApiClient.getPayment(any(PortOnePaymentApiRequest.class))).thenReturn(
                    new PortOnePaymentApiResponse(
                            request.getPaymentId(),
                            "PAID",
                            new PortOnePaymentApiResponse.Amount(BigDecimal.valueOf(10000)),
                            "CARD",
                            "https://receipt.url",
                            "1735689600",
                            "1735689600"
                    ));
            doThrow(new BusinessException(PAYMENT_ALREADY_CANCELED))
                    .when(paymentService).refundPayment(eq(payment), eq(request.getRefundReason()));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationFacade.refundPayment(accountId, request),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_ALREADY_CANCELED, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_ALREADY_CANCELED이어야 합니다."));

            verify(accountService, times(1)).findByAccountId(eq(accountId));
            verify(paymentService, times(1)).findByAccountIdAndPaymentKey(eq(accountId), eq(request.getPaymentId()));
            verify(portOneApiClient, times(1)).getPayment(any(PortOnePaymentApiRequest.class));
            verify(paymentService, times(1)).refundPayment(eq(payment), eq(request.getRefundReason()));
            verify(reservationService, never()).cancelReservation(any(Reservation.class));
        }

    }

}
