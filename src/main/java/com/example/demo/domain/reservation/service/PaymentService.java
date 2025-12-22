package com.example.demo.domain.reservation.service;

import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.reservation.dto.PaymentRequest.PrePaymentRequest;
import com.example.demo.domain.reservation.dto.PaymentResponse.PrePaymentInfoResponse;
import com.example.demo.domain.reservation.dto.PaymentVerifyCommand;
import com.example.demo.domain.reservation.model.Payment;
import com.example.demo.domain.reservation.model.Reservation;
import java.util.UUID;

/**
 * PackageName : com.example.demo.domain.reservation.service
 * FileName    : PaymentService
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : 결제(Payment) 서비스 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
public interface PaymentService {

    PrePaymentInfoResponse savePrePayment(Account account,
                                          Reservation reservation,
                                          PrePaymentRequest request,
                                          String paymentInfo,
                                          String clientIp);

    Payment findByAccountIdAndPaymentKey(UUID accountId,String paymentKey);

    void verifyAndApprove(UUID accountId, PaymentVerifyCommand command, String clientIp);

    void cancelPayment(String paymentKey, String cancelReason);

    void refundPayment(Payment payment, String refundReason);

}
