package com.example.demo.infra.payment.portone.dto;

import com.example.demo.infra.payment.dto.PGPaymentApiBaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.infra.payment.portone.dto
 * FileName    : PortOnePaymentApiRequest
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : 포트원(PortOne) 결제 API 요청 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(name = "포트원(PortOne) 결제 요청 DTO")
public class PortOnePaymentApiBaseRequest extends PGPaymentApiBaseRequest {

    @NotBlank(message = "결제 ID는 필수 입력 값입니다.")
    @Schema(name = "포트원(PortOne) 결제 ID")
    private String paymentKey;

}
