package com.example.demo.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.example.demo.domain.reservation.dto
 * FileName    : PaymentVerifyCommand
 * Author      : oldolgol331
 * Date        : 25. 12. 20.
 * Description : 특정 PG사 종속되지 않는 결제 정보 검증용 표준 객체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 20.   oldolgol331          Initial creation
 */
@Getter
@RequiredArgsConstructor
public class PaymentVerifyCommand {

    @NotBlank
    @Schema(description = "PG사 결제 ID")
    private final String paymentKey;

    @NotNull
    @Schema(description = "결제 금액")
    private final BigDecimal amount;

    @NotBlank
    @Schema(description = "결제 상태")
    private final String status;

    @NotBlank
    @Schema(description = "결제 방법")
    private final String paymentMethod;

    @NotNull
    @Schema(description = "결제 완료 일시")
    private final LocalDateTime paidAt;

    @NotBlank
    @Schema(description = "영수증 URL")
    private final String receiptUrl;

}
