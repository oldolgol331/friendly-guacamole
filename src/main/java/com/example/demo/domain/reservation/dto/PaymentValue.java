package com.example.demo.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.reservation.dto
 * FileName    : PaymentValue
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : 결제 정보 기록
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "결제 정보")
public class PaymentValue implements Serializable {

    @NotNull
    @NotBlank(message = "PG사 결제 ID는 필수 입력 값입니다.")
    @Schema(description = "PG사 결제 ID ")
    private String paymentKey;

    @NotBlank(message = "결제 방법은 필수 입력 값입니다.")
    @Schema(description = "결제 방법")
    private String paymentMethod;

    @Min(value = 0, message = "결제 금액은 0 이상이어야 합니다.")
    @Schema(description = "결제 금액")
    private int price;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "결제 정보 만료 기한")
    private LocalDateTime expiredAt;

}
