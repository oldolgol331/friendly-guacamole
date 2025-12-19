package com.example.demo.infra.payment.portone.client;

import static com.example.demo.common.response.ErrorCode.PAYMENT_API_ERROR;
import static com.example.demo.common.response.ErrorCode.PAYMENT_CANCEL_FAILED;
import static com.example.demo.common.response.ErrorCode.PAYMENT_NOT_FOUND_IN_PG;

import com.example.demo.common.error.BusinessException;
import com.example.demo.infra.payment.client.PGPaymentApiBaseClient;
import com.example.demo.infra.payment.portone.dto.PortOneCancelPaymentApiBaseRequest;
import com.example.demo.infra.payment.portone.dto.PortOnePaymentApiBaseRequest;
import com.example.demo.infra.payment.portone.dto.PortOnePaymentApiBaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
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
public class PortOnePGPaymentApiBaseClient implements PGPaymentApiBaseClient<PortOnePaymentApiBaseRequest,
        PortOnePaymentApiBaseResponse,
        PortOneCancelPaymentApiBaseRequest> {

    private final RestClient restClient;

    public PortOnePGPaymentApiBaseClient(@Qualifier("portOneRestClient") final RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * 포트원 결제 단 건을 조회합니다.
     *
     * @param request - 포트원 결제 요청 DTO
     * @return 포트원 결제 정보 응답 DTO
     */
    @Override
    public PortOnePaymentApiBaseResponse getPayment(final PortOnePaymentApiBaseRequest request) {
        String paymentId = request.getPaymentKey();
        return restClient.get()
                         .uri("/payments/{paymentId}", paymentId)
                         .retrieve()
                         .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                             log.error("PortOne Client Error - paymentId: {}", paymentId);
                             throw new BusinessException(PAYMENT_NOT_FOUND_IN_PG);
                         })
                         .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                             log.error("PortOne Server Error - paymentId: {}", paymentId);
                             throw new BusinessException(PAYMENT_API_ERROR);
                         })
                         .body(PortOnePaymentApiBaseResponse.class);
    }

    /**
     * 포트원 결제 단 건을 취소합니다.
     *
     * @param request - 포트원 결제 취소 요청 DTO
     */
    @Override
    public void cancelPayment(final String paymentId, final PortOneCancelPaymentApiBaseRequest request) {
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
