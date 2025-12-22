package com.example.demo.domain.reservation.service;

import static com.example.demo.common.response.ErrorCode.EXPIRE_PAYMENT_VERIFICATION_TIME;
import static com.example.demo.common.response.ErrorCode.INVALID_CLIENT_IP;
import static com.example.demo.common.response.ErrorCode.PAYMENT_ACCOUNT_MISMATCH;
import static com.example.demo.common.response.ErrorCode.PAYMENT_ALREADY_CANCELED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_AMOUNT_MISMATCH;
import static com.example.demo.common.response.ErrorCode.PAYMENT_KEY_GENERATION_FAILED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_NOT_COMPLETED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_NOT_FOUND;
import static com.example.demo.common.response.ErrorCode.PAYMENT_REFUND_FAILED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_VERIFICATION_FAILED;
import static com.example.demo.common.util.CommonUtils.isAllowedIpRange;
import static com.example.demo.common.util.CommonUtils.isLocalIpAddress;
import static com.example.demo.common.util.CommonUtils.isProxyHeader;
import static com.example.demo.common.util.CommonUtils.isValidIpAddress;
import static com.example.demo.infra.redis.constant.RedisConst.REDIS_PRE_PAYMENT_EXPIRE_MINUTES;
import static com.example.demo.infra.redis.constant.RedisConst.REDIS_PRE_PAYMENT_KEY_PREFIX;
import static java.util.stream.Collectors.joining;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

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
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.example.demo.domain.reservation.service
 * FileName    : PaymentServiceImpl
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : 결제(Payment) 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final int PAYMENT_KEY_GENERATION_MAX_ATTEMPTS = 5;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final PaymentRepository paymentRepository;
    private final RedisRepository   redisRepository;

    /**
     * 결제 사전 정보를 Redis에 저장합니다. 저장한 결제 정보는 PG사의 결제 정보와 비교/검증을 하는데 사용합니다.
     * Redis에 저장이 완료되면 클라이언트에게 전달할 결제 정보를 반환합니다.
     *
     * @param account     - 계정 엔티티
     * @param reservation - 예약 엔티티
     * @param request     - 결제 전 사전 정보 요청 DTO
     * @param paymentInfo - 결제 상품 정보
     * @param clientIp - 클라이언트 IP 주소
     * @return 사전 결제 정보 응답 DTO
     */
    @Override
    public PrePaymentInfoResponse savePrePayment(final Account account,
                                                 final Reservation reservation,
                                                 final PrePaymentRequest request,
                                                 final String paymentInfo,
                                                 final String clientIp) {
        if (!isValidClientIp(clientIp)) throw new BusinessException(INVALID_CLIENT_IP);  // 클라이언트 IP 검증

        LocalDateTime generateTime = LocalDateTime.now();

        String paymentKey;
        do {
            paymentKey = generatePaymentKey(generateTime); // 결제 UUID 생성
        } while (paymentRepository.existsByPaymentKey(paymentKey));
        LocalDateTime expiredAt = generateTime.plusMinutes(REDIS_PRE_PAYMENT_EXPIRE_MINUTES);   // Redis 데이터 만료 시간
        String        redisKey  = getPrePaymentRedisKey(paymentKey);   // Redis 키 생성

        PaymentValue paymentValue = new PaymentValue(paymentKey,
                                                     request.getPaymentMethod(),
                                                     request.getPrice(),
                                                     expiredAt);    // Redis에 기록할 사전 결제 정보

        redisRepository.setValue(redisKey, paymentValue, Duration.ofMinutes(REDIS_PRE_PAYMENT_EXPIRE_MINUTES));

        Payment payment = Payment.of(reservation,
                                     paymentKey,
                                     request.getPaymentMethod(),
                                     paymentInfo,
                                     BigDecimal.valueOf(request.getPrice()),
                                     clientIp);
        paymentRepository.save(payment);

        return new PrePaymentInfoResponse(paymentKey,
                                          request.getPerformanceId(),
                                          request.getSeatId(),
                                          paymentInfo,
                                          request.getPrice(),
                                          expiredAt);
    }

    /**
     * 결제 키로 결제 엔티티를 조회합니다.
     *
     * @param accountId  - 계정 ID
     * @param paymentKey - PG사 결제 ID
     * @return 결제 엔티티
     */
    @Override
    public Payment findByAccountIdAndPaymentKey(final UUID accountId, final String paymentKey) {
        return paymentRepository.findByReservation_AccountIdAndPaymentKey(accountId, paymentKey)
                                .orElseThrow(() -> new BusinessException(PAYMENT_NOT_FOUND));
    }

    /**
     * PG사 결제 데이터와 서버의 결제 정보를 비교/검증하고, 결제 승인 처리합니다.
     *
     * @param accountId - 계정 ID
     * @param command   - 결제 검증용 DTO
     * @param clientIp  - 클라이언트 IP
     */
    @Transactional
    @Override
    public void verifyAndApprove(final UUID accountId, final PaymentVerifyCommand command, final String clientIp) {
        if (!isValidClientIp(clientIp)) throw new BusinessException(INVALID_CLIENT_IP);  // 클라이언트 IP 검증

        String paymentKey = command.getPaymentKey();
        String redisKey   = getPrePaymentRedisKey(paymentKey);

        PaymentValue value = redisRepository.getValue(redisKey, PaymentValue.class)
                                            .orElseThrow(() -> new BusinessException(EXPIRE_PAYMENT_VERIFICATION_TIME));
        verifyPaymentData(command, value);  // 결제 데이터 비교/검증(PG사 결제 데이터 == Redis 사전 저장 결제 데이터)

        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                                           .orElseThrow(() -> new BusinessException(PAYMENT_NOT_FOUND));

        isPaymentInfoMatch(accountId, payment, command);  // 결제 데이터 비교/검증(DB에 저장된 결제 엔티티 데이터 == PG사 결제 데이터)

        payment.approve(command.getPaymentMethod(), command.getPaidAt(), command.getReceiptUrl());

        applicationEventPublisher.publishEvent(new PaymentCompletedEvent(this, payment));

        redisRepository.deleteData(redisKey);
    }

    /**
     * 결제 취소 처리합니다.
     *
     * @param paymentKey   - PG사 결제 ID
     * @param cancelReason - 결제 취소 사유
     */
    @Transactional(propagation = REQUIRES_NEW)
    @Override
    public void cancelPayment(final String paymentKey, final String cancelReason) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                                           .orElseThrow(() -> new BusinessException(PAYMENT_NOT_FOUND));
        payment.cancel(cancelReason);
    }

    /**
     * 결제 환불 처리합니다.
     *
     * @param payment      - 결제 엔티티
     * @param refundReason - 결제 환불 사유
     */
    @Transactional
    @Override
    public void refundPayment(final Payment payment, final String refundReason) {
        switch (payment.getStatus()) {
            case PENDING, FAILED -> throw new BusinessException(PAYMENT_REFUND_FAILED);
            case CANCELLED -> throw new BusinessException(PAYMENT_ALREADY_CANCELED);
        }

        payment.cancel(refundReason);

        applicationEventPublisher.publishEvent(new PaymentCanceledEvent(this, payment, refundReason));
    }

    // ========================= 내부 메서드 =========================

    /**
     * PG사 결제 ID를 생성합니다.
     *
     * @param generateTime - 생성 시간
     * @return 생성된 결제 ID, 엔티티의 PK가 아닌 PG사 결제 ID 값
     */
    private String generatePaymentKey(final LocalDateTime generateTime) {
        for (int attempt = 0; attempt < PAYMENT_KEY_GENERATION_MAX_ATTEMPTS; attempt++) {
            String paymentKey = "Payment:%s:%s".formatted(
                    generateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                    IntStream.range(0, 3)
                             .mapToObj(i -> UUID.randomUUID().toString().replace("-", ""))
                             .collect(joining())
            );

            if (!paymentRepository.existsByPaymentKey(paymentKey)) return paymentKey;

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("결제 키 생성 중 인터럽트 발생", e);
            }
        }

        throw new BusinessException(PAYMENT_KEY_GENERATION_FAILED);
    }

    /**
     * 결제 사전 정보 저장을 위한 임시 Redis 키를 생성합니다.
     *
     * @param paymentKey - PG사 결제 ID
     * @return 결제 사전 정보 저장 Redis 키
     */
    private String getPrePaymentRedisKey(final String paymentKey) {
        return REDIS_PRE_PAYMENT_KEY_PREFIX.formatted(paymentKey);
    }

    /**
     * PG사 결제 데이터와 사전 결제 정보를 비교/검증합니다.
     *
     * @param command - PG사 결제 데이터
     * @param value   - 사전 결제 정보
     */
    private void verifyPaymentData(final PaymentVerifyCommand command, final PaymentValue value) {
        if (!command.getPaymentKey().equals(value.getPaymentKey())
            || command.getAmount().compareTo(BigDecimal.valueOf(value.getPrice())) != 0
            || !command.getPaymentMethod().equals(value.getPaymentMethod()))
            throw new BusinessException(PAYMENT_VERIFICATION_FAILED);
    }

    /**
     * 클라이언트 IP가 유효한지 확인합니다.
     *
     * @param clientIp - 클라이언트 IP
     * @return 클라이언트 IP가 유효한지 여부
     */
    private boolean isValidClientIp(final String clientIp) {
        if (!isValidIpAddress(clientIp)) return false;
        if (isLocalIpAddress(clientIp)) return false;
        if (isProxyHeader(clientIp)) return false;
        if (!isAllowedIpRange(clientIp)) return false;
        return true;
    }

    /**
     * 결제 정보가 일치하는지 확인합니다.
     *
     * @param accountId - 계정 ID
     * @param payment   - 결제 엔티티
     * @param command   - PG사 결제 데이터
     */
    private void isPaymentInfoMatch(final UUID accountId, final Payment payment, final PaymentVerifyCommand command) {
        if (!payment.getReservation().getAccount().getId().equals(accountId))    // 결제 계정 확인
            throw new BusinessException(PAYMENT_ACCOUNT_MISMATCH);
        if (payment.getAmount().compareTo(command.getAmount()) != 0)    // 결제 금액 확인
            throw new BusinessException(PAYMENT_AMOUNT_MISMATCH);
        if (!"PAID".equals(command.getStatus()))    // PG사 결제 상태('PAID') 확인
            throw new BusinessException(PAYMENT_NOT_COMPLETED);

        LocalDateTime now = LocalDateTime.now();
        if (command.getPaidAt().isAfter(now.plusMinutes(5)) || command.getPaidAt().isBefore(now.minusHours(1))) {
            log.warn("결제 시간 이상 감지 - paymentId: {}, paidAt: {}", payment.getPaymentKey(), command.getPaidAt());
            throw new BusinessException(PAYMENT_VERIFICATION_FAILED);
        }

        if (!payment.getPaymentMethod().equals("UNKNOWN")
            && !payment.getPaymentMethod().equals(command.getPaymentMethod())) {
            log.warn("결제 수단 불일치 - paymentId: {}, expected: {}, actual: {}",
                     payment.getPaymentKey(), payment.getPaymentMethod(), command.getPaymentMethod());
            throw new BusinessException(PAYMENT_VERIFICATION_FAILED);
        }
    }

}
