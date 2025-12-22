package com.example.demo.domain.reservation.controller;

import static com.example.demo.common.response.SuccessCode.PAYMENT_REFUND_SUCCESS;
import static com.example.demo.common.response.SuccessCode.PAYMENT_SUCCESS;
import static com.example.demo.common.response.SuccessCode.PRE_PAYMENT_SAVE_SUCCESS;
import static com.example.demo.common.util.CommonUtils.getClientIpAddress;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.SuccessCode;
import com.example.demo.common.security.model.CustomUserDetails;
import com.example.demo.domain.reservation.dto.PaymentRequest.PaymentCancelRequest;
import com.example.demo.domain.reservation.dto.PaymentRequest.PaymentCompleteRequest;
import com.example.demo.domain.reservation.dto.PaymentRequest.PrePaymentRequest;
import com.example.demo.domain.reservation.dto.PaymentResponse.PrePaymentInfoResponse;
import com.example.demo.domain.reservation.facade.ReservationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.example.demo.domain.reservation.controller
 * FileName    : PaymentController
 * Author      : oldolgol331
 * Date        : 25. 12. 20.
 * Description : 결제(Payment) 컨트롤러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 20.   oldolgol331          Initial creation
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "결제 API", description = "PG사 결제 전 서버에 결제 정보 사전 저장, 결제 정보 검증 후 최종 승인 API를 제공합니다.")
public class PaymentController {

    private final ReservationFacade reservationFacade;

    @PostMapping("/prepare")
    @Operation(summary = "검증용 결제 정보 사전 저장", description = "PG사 결제 전 서버에 검증용 결제 정보를 저장하고, PG사 결제 ID를 발급받습니다.")
    public ResponseEntity<ApiResponse<PrePaymentInfoResponse>> savePrePayment(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @Valid @RequestBody final PrePaymentRequest request,
            final HttpServletRequest req
    ) {
        PrePaymentInfoResponse responseData = reservationFacade.savePrePayment(userDetails.getId(),
                                                                               request,
                                                                               getClientIpAddress(req));
        final SuccessCode successCode = PRE_PAYMENT_SAVE_SUCCESS;
        return ResponseEntity.status(successCode.getStatus()).body(ApiResponse.success(successCode, responseData));
    }

    @PostMapping("/complete")
    @Operation(summary = "결제 검증 및 완료", description = "PG사 결제 성공 후 서버에 검증을 요청하고 결제를 확정합니다.")
    public ResponseEntity<ApiResponse<Void>> completePayment(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @Valid @RequestBody final PaymentCompleteRequest request,
            final HttpServletRequest req
    ) {
        reservationFacade.verifyPayment(userDetails.getId(), request.getPaymentId(), getClientIpAddress(req));
        final SuccessCode successCode = PAYMENT_SUCCESS;
        return ResponseEntity.status(successCode.getStatus()).body(ApiResponse.success(successCode));
    }

    @PostMapping("/refund")
    @Operation(summary = "결제 취소", description = "클라이언트가 결제 내역을 취소하고 환불 받는 기능을 제공합니다.")
    public ResponseEntity<ApiResponse<Void>> refundPayment(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @Valid @RequestBody final PaymentCancelRequest request
    ) {
        reservationFacade.refundPayment(userDetails.getId(), request);
        final SuccessCode successCode = PAYMENT_REFUND_SUCCESS;
        return ResponseEntity.status(successCode.getStatus()).body(ApiResponse.success(successCode));
    }

}
