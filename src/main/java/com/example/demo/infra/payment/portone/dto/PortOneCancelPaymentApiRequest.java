package com.example.demo.infra.payment.portone.dto;

import com.example.demo.infra.payment.dto.PGCancelPaymentApiBaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.infra.payment.portone.dto
 * FileName    : PortOneCancelPaymentApiRequest
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : 포트원(PortOne) 결제 취소 API 요청 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(name = "포트원(PortOne) 결제 취소 요청 DTO")
public class PortOneCancelPaymentApiRequest extends PGCancelPaymentApiBaseRequest {

    @Schema(description = "PG사 결제 ID")
    private String paymentId;

    @Schema(description = "취소 금액")
    private int amount;

    @Schema(description = "취소 사유")
    private String reason;

}
