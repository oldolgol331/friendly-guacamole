package com.example.demo.domain.reservation.service;

import static com.example.demo.common.response.ErrorCode.EXPIRE_PAYMENT_VERIFICATION_TIME;
import static com.example.demo.common.response.ErrorCode.INVALID_CLIENT_IP;
import static com.example.demo.common.response.ErrorCode.PAYMENT_ACCOUNT_MISMATCH;
import static com.example.demo.common.response.ErrorCode.PAYMENT_ALREADY_CANCELED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_AMOUNT_MISMATCH;
import static com.example.demo.common.response.ErrorCode.PAYMENT_NOT_COMPLETED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_NOT_FOUND;
import static com.example.demo.common.response.ErrorCode.PAYMENT_REFUND_FAILED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_VERIFICATION_FAILED;
import static com.example.demo.common.util.TestUtils.FAKER;
import static com.example.demo.common.util.TestUtils.createAccount;
import static com.example.demo.common.util.TestUtils.createPayment;
import static com.example.demo.common.util.TestUtils.createPerformance;
import static com.example.demo.common.util.TestUtils.createReservation;
import static com.example.demo.common.util.TestUtils.createSeat;
import static com.example.demo.common.util.TestUtils.generateIpAddress;
import static com.example.demo.domain.reservation.model.PaymentStatus.CANCELLED;
import static com.example.demo.domain.reservation.model.PaymentStatus.PAID;
import static com.example.demo.domain.reservation.model.PaymentStatus.PENDING;
import static com.example.demo.infra.redis.constant.RedisConst.REDIS_PRE_PAYMENT_EXPIRE_MINUTES;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.common.error.BusinessException;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.reservation.dao.PaymentRepository;
import com.example.demo.domain.reservation.dto.PaymentRequest.PrePaymentRequest;
import com.example.demo.domain.reservation.dto.PaymentResponse.PrePaymentInfoResponse;
import com.example.demo.domain.reservation.dto.PaymentValue;
import com.example.demo.domain.reservation.dto.PaymentVerifyCommand;
import com.example.demo.domain.reservation.event.PaymentCanceledEvent;
import com.example.demo.domain.reservation.event.PaymentCompletedEvent;
import com.example.demo.domain.reservation.model.Payment;
import com.example.demo.domain.reservation.model.Reservation;
import com.example.demo.infra.redis.dao.RedisRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * PackageName : com.example.demo.domain.reservation.service
 * FileName    : PaymentServiceTest
 * Author      : oldolgol331
 * Date        : 25. 12. 22.
 * Description : PaymentService 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 22.   oldolgol331          Initial creation
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    PaymentServiceImpl        paymentService;
    @Mock
    ApplicationEventPublisher applicationEventPublisher;
    @Mock
    PaymentRepository         paymentRepository;
    @Mock
    RedisRepository           redisRepository;

    private String generatePaymentKey() {
        return "Payment:%s:%s".formatted(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                IntStream.range(0, 3)
                         .mapToObj(i -> UUID.randomUUID().toString().replace("-", ""))
                         .collect(joining())
        );
    }

    @Nested
    @DisplayName("savePrePayment() 테스트")
    class SavePrePaymentTests {

        @RepeatedTest(10)
        @DisplayName("사전 결제 정보 저장")
        void savePrePayment() {
            // given
            Account           account     = createAccount();
            Reservation       reservation = createReservation(account, createSeat(createPerformance()));
            PrePaymentRequest request     = new PrePaymentRequest(1L, 1L, "CARD", 10000);
            String            paymentInfo = FAKER.commerce().productName();
            String            clientIp    = generateIpAddress();

            LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(REDIS_PRE_PAYMENT_EXPIRE_MINUTES);

            when(paymentRepository.existsByPaymentKey(anyString())).thenReturn(false);
            doNothing().when(redisRepository).setValue(anyString(), any(PaymentValue.class), any(Duration.class));
            when(paymentRepository.save(any(Payment.class))).thenReturn(createPayment(reservation));

            // when
            PrePaymentInfoResponse response = paymentService.savePrePayment(
                    account, reservation, request, paymentInfo, clientIp
            );

            // then
            assertAll(() -> assertNotNull(response.getPaymentKey()),
                      () -> assertEquals(request.getPerformanceId(), response.getPerformanceId()),
                      () -> assertEquals(request.getSeatId(), response.getSeatId()),
                      () -> assertEquals(request.getPrice(), response.getPrice()),
                      () -> assertEquals(expiredAt.withNano(0), response.getExpiredAt().withNano(0)));

            verify(paymentRepository, times(1)).existsByPaymentKey(anyString());
            verify(redisRepository, times(1)).setValue(anyString(), any(PaymentValue.class), any(Duration.class));
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }

        @RepeatedTest(10)
        @DisplayName("사전 결제 정보 저장 시도, 유효하지 않은 클라이언트 IP")
        void savePrePayment_invalidClientIp() {
            // given
            Account           account     = createAccount();
            Reservation       reservation = createReservation(account, createSeat(createPerformance()));
            PrePaymentRequest request     = new PrePaymentRequest(1L, 1L, "CARD", 10000);
            String            paymentInfo = FAKER.commerce().productName();
            String            clientIp    = "192.168.0.0"; // 유효하지 않은 IP

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.savePrePayment(account,
                                                                                           reservation,
                                                                                           request,
                                                                                           paymentInfo,
                                                                                           clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(INVALID_CLIENT_IP, exception.getErrorCode(),
                                         "errorCode는 INVALID_CLIENT_IP여야 합니다."));

            verify(paymentRepository, never()).existsByPaymentKey(anyString());
            verify(redisRepository, never()).setValue(anyString(), any(PaymentValue.class), any(Duration.class));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

    }

    @Nested
    @DisplayName("findByAccountIdAndPaymentKey() 테스트")
    class FindByAccountIdAndPaymentKeyTests {

        @RepeatedTest(10)
        @DisplayName("계정 ID와 결제 키로 결제 정보 조회")
        void findByAccountIdAndPaymentKey() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            Payment payment = createPayment(createReservation(createAccount(),
                                                              createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);

            when(paymentRepository.findByReservation_AccountIdAndPaymentKey(eq(accountId), eq(paymentKey)))
                    .thenReturn(Optional.of(payment));

            // when
            Payment result = paymentService.findByAccountIdAndPaymentKey(accountId, paymentKey);

            // then
            assertEquals(payment, result);

            verify(paymentRepository, times(1)).findByReservation_AccountIdAndPaymentKey(eq(accountId), eq(paymentKey));
        }

        @RepeatedTest(10)
        @DisplayName("계정 ID와 결제 키로 결제 정보 조회 시도, 해당 결제가 없음")
        void findByAccountIdAndPaymentKey_notFound() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();

            when(paymentRepository.findByReservation_AccountIdAndPaymentKey(eq(accountId), eq(paymentKey)))
                    .thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.findByAccountIdAndPaymentKey(accountId,
                                                                                                         paymentKey),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_NOT_FOUND여야 합니다."));

            verify(paymentRepository, times(1)).findByReservation_AccountIdAndPaymentKey(eq(accountId), eq(paymentKey));
        }

    }

    @Nested
    @DisplayName("verifyAndApprove() 테스트")
    class VerifyAndApproveTests {

        @RepeatedTest(10)
        @DisplayName("결제 검증 및 승인")
        void verifyAndApprove() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();
            PaymentVerifyCommand command = new PaymentVerifyCommand(
                    paymentKey,
                    BigDecimal.valueOf(10000),
                    "PAID",
                    "CARD",
                    LocalDateTime.now(),
                    "https://receipt.url"
            );

            PaymentValue paymentValue = new PaymentValue(
                    paymentKey, "CARD", 10000, LocalDateTime.now().plusMinutes(10)
            );
            Account account = createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            Payment payment = createPayment(createReservation(account, createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);
            ReflectionTestUtils.setField(payment, "amount", BigDecimal.valueOf(10000));
            ReflectionTestUtils.setField(payment, "status", PENDING);

            when(redisRepository.getValue(anyString(), eq(PaymentValue.class))).thenReturn(Optional.of(paymentValue));
            when(paymentRepository.findByPaymentKey(eq(paymentKey))).thenReturn(Optional.of(payment));
            when(redisRepository.deleteData(anyString())).thenReturn(true);

            // when
            paymentService.verifyAndApprove(accountId, command, clientIp);

            // then
            assertEquals(PAID, payment.getStatus());

            verify(redisRepository, times(1)).getValue(anyString(), eq(PaymentValue.class));
            verify(paymentRepository, times(1)).findByPaymentKey(eq(paymentKey));
            verify(applicationEventPublisher, times(1)).publishEvent(any(PaymentCompletedEvent.class));
            verify(redisRepository, times(1)).deleteData(anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 및 승인 시도, 유효하지 않은 클라이언트 IP")
        void verifyAndApprove_invalidClientIp() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = "192.168.0.0"; // 유효하지 않은 IP
            PaymentVerifyCommand command = new PaymentVerifyCommand(
                    paymentKey,
                    BigDecimal.valueOf(10000),
                    "PAID",
                    "CARD",
                    LocalDateTime.now(),
                    "https://receipt.url"
            );

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.verifyAndApprove(accountId,
                                                                                             command,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(INVALID_CLIENT_IP, exception.getErrorCode(),
                                         "errorCode는 INVALID_CLIENT_IP여야 합니다."));

            verify(redisRepository, never()).getValue(anyString(), eq(PaymentValue.class));
            verify(paymentRepository, never()).findByPaymentKey(anyString());
            verify(applicationEventPublisher, never()).publishEvent(any(PaymentCompletedEvent.class));
            verify(redisRepository, never()).deleteData(anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 및 승인 시도, 결제 검증 시간이 만료됨")
        void verifyAndApprove_expiredVerificationTime() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();
            PaymentVerifyCommand command = new PaymentVerifyCommand(
                    paymentKey,
                    BigDecimal.valueOf(10000),
                    "PAID",
                    "CARD",
                    LocalDateTime.now(),
                    "https://receipt.url"
            );

            when(redisRepository.getValue(anyString(), eq(PaymentValue.class))).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.verifyAndApprove(accountId,
                                                                                             command,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(EXPIRE_PAYMENT_VERIFICATION_TIME, exception.getErrorCode(),
                                         "errorCode는 EXPIRE_PAYMENT_VERIFICATION_TIME이어야 합니다."));

            verify(redisRepository, times(1)).getValue(anyString(), eq(PaymentValue.class));
            verify(paymentRepository, never()).findByPaymentKey(anyString());
            verify(applicationEventPublisher, never()).publishEvent(any(PaymentCompletedEvent.class));
            verify(redisRepository, never()).deleteData(anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 및 승인 시도, 결제 정보가 일치하지 않음")
        void verifyAndApprove_paymentVerificationFailed() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();
            PaymentVerifyCommand command = new PaymentVerifyCommand(
                    paymentKey,
                    BigDecimal.valueOf(15000), // 금액이 일치하지 않음
                    "PAID",
                    "CARD",
                    LocalDateTime.now(),
                    "https://receipt.url"
            );

            PaymentValue paymentValue = new PaymentValue(
                    paymentKey, "CARD", 10000, LocalDateTime.now().plusMinutes(10)
            );
            Payment payment = createPayment(createReservation(createAccount(),
                                                              createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);
            ReflectionTestUtils.setField(payment, "amount", BigDecimal.valueOf(1000));

            when(redisRepository.getValue(anyString(), eq(PaymentValue.class))).thenReturn(Optional.of(paymentValue));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.verifyAndApprove(accountId,
                                                                                             command,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_VERIFICATION_FAILED, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_VERIFICATION_FAILED이어야 합니다."));

            verify(redisRepository, times(1)).getValue(anyString(), eq(PaymentValue.class));
            verify(paymentRepository, never()).findByPaymentKey(eq(paymentKey));
            verify(applicationEventPublisher, never()).publishEvent(any(PaymentCompletedEvent.class));
            verify(redisRepository, never()).deleteData(anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 및 승인 시도, 결제가 존재하지 않음")
        void verifyAndApprove_paymentNotFound() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();
            PaymentVerifyCommand command = new PaymentVerifyCommand(
                    paymentKey,
                    BigDecimal.valueOf(10000),
                    "PAID",
                    "CARD",
                    LocalDateTime.now(),
                    "https://receipt.url"
            );

            PaymentValue paymentValue = new PaymentValue(
                    paymentKey, "CARD", 10000, LocalDateTime.now().plusMinutes(10)
            );

            when(redisRepository.getValue(anyString(), eq(PaymentValue.class))).thenReturn(Optional.of(paymentValue));
            when(paymentRepository.findByPaymentKey(eq(paymentKey))).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.verifyAndApprove(accountId,
                                                                                             command,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_NOT_FOUND이어야 합니다."));

            verify(redisRepository, times(1)).getValue(anyString(), eq(PaymentValue.class));
            verify(paymentRepository, times(1)).findByPaymentKey(eq(paymentKey));
            verify(applicationEventPublisher, never()).publishEvent(any(PaymentCompletedEvent.class));
            verify(redisRepository, never()).deleteData(anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 및 승인 시도, 계정 불일치")
        void verifyAndApprove_accountMismatch() {
            // given
            UUID   accountId      = UUID.randomUUID();
            UUID   otherAccountId = UUID.randomUUID();
            String paymentKey     = generatePaymentKey();
            String clientIp       = generateIpAddress();
            PaymentVerifyCommand command = new PaymentVerifyCommand(
                    paymentKey,
                    BigDecimal.valueOf(10000),
                    "PAID",
                    "CARD",
                    LocalDateTime.now(),
                    "https://receipt.url"
            );

            PaymentValue paymentValue = new PaymentValue(
                    paymentKey, "CARD", 10000, LocalDateTime.now().plusMinutes(10)
            );
            Payment payment = createPayment(createReservation(createAccount(),
                                                              createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);
            ReflectionTestUtils.setField(payment.getReservation().getAccount(), "id", otherAccountId);

            when(redisRepository.getValue(anyString(), eq(PaymentValue.class))).thenReturn(Optional.of(paymentValue));
            when(paymentRepository.findByPaymentKey(eq(paymentKey))).thenReturn(Optional.of(payment));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.verifyAndApprove(accountId, command,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_ACCOUNT_MISMATCH, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_ACCOUNT_MISMATCH이어야 합니다."));

            verify(redisRepository, times(1)).getValue(anyString(), eq(PaymentValue.class));
            verify(paymentRepository, times(1)).findByPaymentKey(eq(paymentKey));
            verify(applicationEventPublisher, never()).publishEvent(any(PaymentCompletedEvent.class));
            verify(redisRepository, never()).deleteData(anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 및 승인 시도, 결제 금액 불일치")
        void verifyAndApprove_amountMismatch() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();
            PaymentVerifyCommand command = new PaymentVerifyCommand(
                    paymentKey,
                    BigDecimal.valueOf(10000),
                    "PAID",
                    "CARD",
                    LocalDateTime.now(),
                    "https://receipt.url"
            );

            PaymentValue paymentValue = new PaymentValue(
                    paymentKey, "CARD", 10000, LocalDateTime.now().plusMinutes(10)
            );
            Account account = createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            Payment payment = createPayment(createReservation(account, createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);
            ReflectionTestUtils.setField(payment, "amount", BigDecimal.valueOf(15000)); // 금액이 일치하지 않음

            when(redisRepository.getValue(anyString(), eq(PaymentValue.class))).thenReturn(Optional.of(paymentValue));
            when(paymentRepository.findByPaymentKey(eq(paymentKey))).thenReturn(Optional.of(payment));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.verifyAndApprove(accountId,
                                                                                             command,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_AMOUNT_MISMATCH, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_AMOUNT_MISMATCH이어야 합니다."));

            verify(redisRepository, times(1)).getValue(anyString(), eq(PaymentValue.class));
            verify(paymentRepository, times(1)).findByPaymentKey(eq(paymentKey));
            verify(applicationEventPublisher, never()).publishEvent(any(PaymentCompletedEvent.class));
            verify(redisRepository, never()).deleteData(anyString());
        }

        @RepeatedTest(10)
        @DisplayName("결제 검증 및 승인 시도, 결제가 완료되지 않음")
        void verifyAndApprove_notCompleted() {
            // given
            UUID   accountId  = UUID.randomUUID();
            String paymentKey = generatePaymentKey();
            String clientIp   = generateIpAddress();
            PaymentVerifyCommand command = new PaymentVerifyCommand(
                    paymentKey,
                    BigDecimal.valueOf(10000),
                    "PENDING", // 결제 상태가 완료되지 않음
                    "CARD",
                    LocalDateTime.now(),
                    "https://receipt.url"
            );

            PaymentValue paymentValue = new PaymentValue(
                    paymentKey, "CARD", 10000, LocalDateTime.now().plusMinutes(10)
            );
            Account account = createAccount();
            ReflectionTestUtils.setField(account, "id", accountId);
            Payment payment = createPayment(createReservation(account, createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);
            ReflectionTestUtils.setField(payment, "amount", BigDecimal.valueOf(10000));

            when(redisRepository.getValue(anyString(), eq(PaymentValue.class))).thenReturn(Optional.of(paymentValue));
            when(paymentRepository.findByPaymentKey(eq(paymentKey))).thenReturn(Optional.of(payment));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.verifyAndApprove(accountId, command,
                                                                                             clientIp),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_NOT_COMPLETED, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_NOT_COMPLETED이어야 합니다."));

            verify(redisRepository, times(1)).getValue(anyString(), eq(PaymentValue.class));
            verify(paymentRepository, times(1)).findByPaymentKey(eq(paymentKey));
            verify(applicationEventPublisher, never()).publishEvent(any(PaymentCompletedEvent.class));
            verify(redisRepository, never()).deleteData(anyString());
        }

    }

    @Nested
    @DisplayName("cancelPayment() 테스트")
    class CancelPaymentTests {

        @RepeatedTest(10)
        @DisplayName("결제 취소")
        void cancelPayment() {
            // given
            String paymentKey   = generatePaymentKey();
            String cancelReason = "고객 요청";
            Payment payment = createPayment(createReservation(createAccount(),
                                                              createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);
            ReflectionTestUtils.setField(payment, "status", PAID);

            when(paymentRepository.findByPaymentKey(eq(paymentKey))).thenReturn(Optional.of(payment));

            // when
            paymentService.cancelPayment(paymentKey, cancelReason);

            // then
            assertEquals(CANCELLED, payment.getStatus());
            assertEquals(cancelReason, payment.getCancelReason());

            verify(paymentRepository, times(1)).findByPaymentKey(eq(paymentKey));
        }

        @RepeatedTest(10)
        @DisplayName("결제 취소 시도, 결제가 존재하지 않음")
        void cancelPayment_notFound() {
            // given
            String paymentKey   = generatePaymentKey();
            String cancelReason = "고객 요청";

            when(paymentRepository.findByPaymentKey(eq(paymentKey))).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.cancelPayment(paymentKey, cancelReason),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_NOT_FOUND이어야 합니다."));

            verify(paymentRepository, times(1)).findByPaymentKey(eq(paymentKey));
        }

    }

    @Nested
    @DisplayName("refundPayment() 테스트")
    class RefundPaymentTests {

        @RepeatedTest(10)
        @DisplayName("결제 환불")
        void refundPayment() {
            // given
            Payment payment = createPayment(createReservation(createAccount(),
                                                              createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);
            ReflectionTestUtils.setField(payment, "status", PAID);
            String refundReason = "고객 요청";

            // when
            paymentService.refundPayment(payment, refundReason);

            // then
            assertEquals(CANCELLED, payment.getStatus());
            assertEquals(refundReason, payment.getCancelReason());

            verify(applicationEventPublisher, times(1)).publishEvent(any(PaymentCanceledEvent.class));
        }

        @RepeatedTest(10)
        @DisplayName("결제 환불 시도, 결제가 미완료 상태")
        void refundPayment_pending() {
            // given
            Payment payment = createPayment(createReservation(createAccount(),
                                                              createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);
            ReflectionTestUtils.setField(payment, "status", PENDING);
            String refundReason = "고객 요청";

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.refundPayment(payment, refundReason),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_REFUND_FAILED, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_REFUND_FAILED이어야 합니다."));

            verify(applicationEventPublisher, never()).publishEvent(any(PaymentCanceledEvent.class));
        }

        @RepeatedTest(10)
        @DisplayName("결제 환불 시도, 결제가 이미 취소됨")
        void refundPayment_alreadyCanceled() {
            // given
            Payment payment = createPayment(createReservation(createAccount(),
                                                              createSeat(createPerformance())));
            ReflectionTestUtils.setField(payment, "id", 1L);
            ReflectionTestUtils.setField(payment, "status", CANCELLED);
            String refundReason = "고객 요청";

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> paymentService.refundPayment(payment, refundReason),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PAYMENT_ALREADY_CANCELED, exception.getErrorCode(),
                                         "errorCode는 PAYMENT_ALREADY_CANCELED이어야 합니다."));

            verify(applicationEventPublisher, never()).publishEvent(any(PaymentCanceledEvent.class));
        }

    }

}
