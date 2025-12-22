package com.example.demo.domain.reservation.dto;

import static lombok.AccessLevel.PRIVATE;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.example.demo.domain.reservation.dto
 * FileName    : PaymentResponse
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : 결제 도메인 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(name = "결제 도메인 응답 DTO")
public abstract class PaymentResponse {

    @Getter
    @RequiredArgsConstructor
    @Schema(name = "사전 결제 정보 응답 DTO")
    public static class PrePaymentInfoResponse {

        @Schema(description = "PG사 결제 ID")
        private final String        paymentKey;
        @Schema(description = "공연 ID")
        private final Long          performanceId;
        @Schema(description = "좌석 ID")
        private final Long          seatId;
        @Schema(description = "결제 상품 정보: {공연명} {좌석 번호}")
        private final String        paymentInfo;
        @Schema(description = "결제 금액")
        private final int           price;
        @Schema(description = "결제 정보 만료 기한")
        private final LocalDateTime expiredAt;

    }

}
