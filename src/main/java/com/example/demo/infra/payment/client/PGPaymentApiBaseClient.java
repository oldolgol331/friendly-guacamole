package com.example.demo.infra.payment.client;

import com.example.demo.infra.payment.dto.PGCancelPaymentApiBaseRequest;
import com.example.demo.infra.payment.dto.PGPaymentApiBaseRequest;
import com.example.demo.infra.payment.dto.PGPaymentApiBaseResponse;

/**
 * PackageName : com.example.demo.infra.payment.client
 * FileName    : PGPaymentApiBaseClient
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : PG사 결제 API 클라이언트 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.    oldolgol331         Initial creation
 */
public interface PGPaymentApiBaseClient<T extends PGPaymentApiBaseRequest,
        R extends PGPaymentApiBaseResponse,
        C extends PGCancelPaymentApiBaseRequest> {

    R getPayment(T request);

    void cancelPayment(String paymentId, C request);

}
