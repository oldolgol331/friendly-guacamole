package com.example.demo.domain.reservation.dto;

import static lombok.AccessLevel.PRIVATE;

import com.example.demo.domain.reservation.model.Reservation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.reservation.dto
 * FileName    : ReservationResponse
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : 예약 도메인 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(name = "예약 도메인 응답 DTO")
public abstract class ReservationResponse {

    @Getter
    @Schema(name = "예약 정보 응답 DTO")
    public static class ReservationInfoResponse {

        @JsonProperty("performance_id")
        @Schema(name = "공연 ID")
        private final Long          performanceId;
        @Schema(name = "좌석 ID")
        private final Long   seatId;
        @JsonProperty("reservation_account_id")
        @Schema(name = "예약자 ID")
        private final UUID   accountId;
        @Schema(name = "예약자 닉네임")
        private final String nickname;
        @JsonProperty("performance_name")
        @Schema(name = "공연 이름")
        private final String        performanceName;
        @JsonProperty("start_time")
        @Schema(name = "공연 시작 시간")
        private final LocalDateTime startTime;
        @JsonProperty("end_time")
        @Schema(name = "공연 종료 시간")
        private final LocalDateTime endTime;
        @JsonProperty("seat_code")
        @Schema(name = "좌석 번호")
        private final String        seatCode;
        @Schema(name = "가격")
        private final int           price;
        @JsonProperty("reservation_time")
        @Schema(name = "예약 확정 시간")
        private final LocalDateTime reservationTime;

        @QueryProjection
        @JsonCreator
        public ReservationInfoResponse(@JsonProperty("performance_id") final Long performanceId,
                                       @JsonProperty("seat_id") final Long seatId,
                                       @JsonProperty("account_id") final UUID accountId,
                                       @JsonProperty("nickname") final String nickname,
                                       @JsonProperty("performance_name") final String performanceName,
                                       @JsonProperty("start_time") final LocalDateTime startTime,
                                       @JsonProperty("end_time") final LocalDateTime endTime,
                                       @JsonProperty("seat_code") final String seatCode,
                                       @JsonProperty("price") final int price,
                                       @JsonProperty("reservation_time") final LocalDateTime reservationTime) {
            this.performanceId = performanceId;
            this.seatId = seatId;
            this.accountId = accountId;
            this.nickname = nickname;
            this.performanceName = performanceName;
            this.startTime = startTime;
            this.endTime = endTime;
            this.seatCode = seatCode;
            this.price = price;
            this.reservationTime = reservationTime;
        }

        public static ReservationInfoResponse from(final Reservation reservation) {
            return new ReservationInfoResponse(reservation.getSeat().getPerformance().getId(),
                                               reservation.getSeatId(),
                                               reservation.getAccountId(),
                                               reservation.getAccount().getNickname(),
                                               reservation.getSeat().getPerformance().getName(),
                                               reservation.getSeat().getPerformance().getStartTime(),
                                               reservation.getSeat().getPerformance().getEndTime(),
                                               reservation.getSeat().getSeatCode(),
                                               reservation.getSeat().getPrice(),
                                               reservation.getReservationTime());
        }

    }

}
