package com.example.demo.domain.performance.dto;

import static lombok.AccessLevel.PRIVATE;

import com.example.demo.domain.performance.model.Seat;
import com.example.demo.domain.performance.model.SeatStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.performance.dto
 * FileName    : SeatResponse
 * Author      : oldolgol331
 * Date        : 26. 1. 8.
 * Description : 좌석 도메인 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 1. 8.     oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(name = "좌석 도메인 응답 DTO")
public abstract class SeatResponse {

    @Getter
    @Schema(name = "좌석 목록 정보 응답 DTO")
    public static class SeatListResponse {

        @Schema(description = "좌석 ID")
        private final Long       id;
        @Schema(description = "좌석 번호")
        private final String     seatCode;
        @Schema(description = "좌석 가격")
        private final int        price;
        @Schema(description = "좌석 상태")
        private final SeatStatus status;

        public SeatListResponse(@JsonProperty("id") final Long id,
                                @JsonProperty("seat_code") final String seatCode,
                                @JsonProperty("price") final int price,
                                @JsonProperty("seat_status") final SeatStatus status) {
            this.id = id;
            this.seatCode = seatCode;
            this.price = price;
            this.status = status;
        }

        public static SeatListResponse from(final Seat seat) {
            return new SeatListResponse(seat.getId(), seat.getSeatCode(), seat.getPrice(), seat.getStatus());
        }

    }

}
