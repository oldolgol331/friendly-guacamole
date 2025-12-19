package com.example.demo.domain.reservation.dto;

import static lombok.AccessLevel.PRIVATE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.reservation.dto
 * FileName    : ReservationRequest
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : 예약 도메인 요청 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(name = "예약 도메인 요청 DTO")
public abstract class ReservationRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "예약 생성 요청 DTO")
    public static class ReservationCreateRequest {

        @Min(value = 1, message = "좌석 ID는 1 이상이어야 합니다.")
        @Schema(name = "좌석 ID")
        private long seatId;

    }

}
