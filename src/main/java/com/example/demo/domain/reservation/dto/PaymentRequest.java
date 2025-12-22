package com.example.demo.domain.reservation.dto;

import static lombok.AccessLevel.PRIVATE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.reservation.dto
 * FileName    : PaymentRequest
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : 결제 도메인 요청 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(name = "결제 도메인 요청 DTO")
public abstract class PaymentRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "결제 전 사전 정보 요청 DTO")
    public static class PrePaymentRequest {

        @NotNull
        @Min(value = 1, message = "공연 ID는 1 이상이어야 합니다.")
        @Schema(description = "공연 ID")
        private Long performanceId;

        @NotNull
        @Min(value = 1, message = "좌석 ID는 1 이상이어야 합니다.")
        @Schema(description = "좌석 ID")
        private Long seatId;

        @NotBlank(message = "결제 방법은 필수 입력 값입니다.")
        @Schema(description = "결제 방법")
        private String paymentMethod;

        @Min(value = 0, message = "결제 금액은 0 이상이어야 합니다.")
        @Schema(description = "결제 금액")
        private int price;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "결제 완료(검증) 요청 DTO")
    public static class PaymentCompleteRequest {

        @NotBlank(message = "PG사 결제 ID는 필수입니다.")
        @Schema(description = "서버에서 발급했던 PG사 결제 ID")
        private String paymentId;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "결제 취소(환불) 요청 DTO")
    public static class PaymentCancelRequest {

        @NotBlank(message = "PG사 결제 ID는 필수입니다.")
        @Schema(description = "PG사 결제 ID")
        private String paymentId;

        @NotBlank(message = "환불 사유는 필수입니다.")
        @Schema(description = "환불 사유")
        private String refundReason;

    }

}
