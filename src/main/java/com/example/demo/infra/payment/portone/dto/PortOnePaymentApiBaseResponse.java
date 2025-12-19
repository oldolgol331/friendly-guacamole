package com.example.demo.infra.payment.portone.dto;

import com.example.demo.infra.payment.dto.PGPaymentApiBaseResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.infra.payment.portone.dto
 * FileName    : PortOnePaymentApiResponse
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : 포트원(PortOne) 결제 API 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "포트원(PortOne) 결제 응답 DTO")
public class PortOnePaymentApiBaseResponse extends PGPaymentApiBaseResponse {

    @Schema(name = "포트원(PortOne) 결제 ID")
    private String id;
    @Schema(name = "결제 상태")
    private String status;
    @Schema(name = "결제 금액 정보")
    private Amount amount;
    @Schema(name = "결제 수단")
    private String method;
    @Schema(name = "영수증 URL")
    private String receiptUrl;
    @Schema(name = "결제 요청 일시")
    private String requestedAt;
    @Schema(name = "결제 완료 일시")
    private String paidAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "포트원(PortOne) 결제 금액")
    public static class Amount {
        @Schema(name = "총 결제 금액")
        private BigDecimal total;
    }

}
