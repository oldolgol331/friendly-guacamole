package com.example.demo.infra.payment.portone.client;

import static com.example.demo.common.response.ErrorCode.PAYMENT_API_ERROR;
import static com.example.demo.common.response.ErrorCode.PAYMENT_CANCEL_FAILED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_NOT_FOUND_IN_PG;

import com.example.demo.common.error.BusinessException;
import com.example.demo.infra.payment.client.PGPaymentApiBaseClient;
import com.example.demo.infra.payment.portone.dto.PortOneCancelPaymentApiRequest;
import com.example.demo.infra.payment.portone.dto.PortOnePaymentApiRequest;
import com.example.demo.infra.payment.portone.dto.PortOnePaymentApiResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * PackageName : com.example.demo.infra.payment.portone.client
 * FileName    : PortOnePaymentClient
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : 포트원(PortOne) 결제 API 클라이언트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
@Component
@Slf4j
public class PortOnePGPaymentApiClient implements PGPaymentApiBaseClient<PortOnePaymentApiRequest,
        PortOnePaymentApiResponse,
        PortOneCancelPaymentApiRequest> {

    private final RestClient    restClient;
    private final MeterRegistry meterRegistry;

    public PortOnePGPaymentApiClient(@Qualifier("portOneRestClient") final RestClient restClient,
                                     final MeterRegistry meterRegistry) {
        this.restClient = restClient;
        this.meterRegistry = meterRegistry;
    }

    /**
     * 포트원 결제 단 건을 조회합니다.
     *
     * @param request - 포트원 결제 요청 DTO
     * @return 포트원 결제 정보 응답 DTO
     */
    @Override
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public PortOnePaymentApiResponse getPayment(final PortOnePaymentApiRequest request) {
        String paymentId = request.getPaymentKey();
        Sample sample    = Timer.start(meterRegistry);

        try {
            return restClient.get()
                             .uri("/payments/{paymentId}", paymentId)
                             .retrieve()
                             .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                                 log.error("PortOne Client Error - paymentId: {}", paymentId);
                                 meterRegistry.counter("payment.api.error", "type", "client", "paymentId", paymentId)
                                              .increment();
                                 throw new BusinessException(PAYMENT_NOT_FOUND_IN_PG);
                             })
                             .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                                 log.error("PortOne Server Error - paymentId: {}", paymentId);
                                 meterRegistry.counter("payment.api.error", "type", "general", "paymentId", paymentId)
                                              .increment();
                                 throw new BusinessException(PAYMENT_API_ERROR);
                             })
                             .body(PortOnePaymentApiResponse.class);
        } catch (Exception e) {
            sample.stop(Timer.builder("payment.api.duration").tag("status", "error").register(meterRegistry));
            throw e;
        }

    }

    /**
     * 포트원 결제 단 건을 취소합니다.
     *
     * @param request - 포트원 결제 취소 요청 DTO
     */
    @Override
    public void cancelPayment(final String paymentId, final PortOneCancelPaymentApiRequest request) {
        restClient.post()
                  .uri("/payments/{paymentId}/cancel", paymentId)
                  .body(request)
                  .retrieve()
                  .onStatus(HttpStatusCode::isError, (req, res) -> {
                      log.error("PortOne Cancel Error - paymentId: {}", paymentId);
                      throw new BusinessException(PAYMENT_CANCEL_FAILED);
                  })
                  .toBodilessEntity();
    }

}
