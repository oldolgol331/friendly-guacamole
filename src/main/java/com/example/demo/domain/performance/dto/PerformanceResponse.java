package com.example.demo.domain.performance.dto;

import static com.example.demo.domain.seat.model.SeatStatus.AVAILABLE;
import static lombok.AccessLevel.PRIVATE;

import com.example.demo.domain.performance.model.Performance;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.performance.dto
 * FileName    : PerformanceResponse
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 공연 도메인 응답 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(name = "공연 도메인 응답 DTO")
public abstract class PerformanceResponse {

    @Getter
    @Schema(name = "공연 목록 정보 응답 DTO")
    public static class PerformanceListResponse {

        @Schema(name = "공연 ID")
        private final Long          id;
        @Schema(name = "공연 이름")
        private final String        name;
        @Schema(name = "공연 장소")
        private final String        venue;
        @JsonProperty("start_time")
        @Schema(name = "공연 시작 시간")
        private final LocalDateTime startTime;
        @JsonProperty("end_time")
        @Schema(name = "공연 종료 시간")
        private final LocalDateTime endTime;
        @JsonProperty("remaining_seats")
        @Schema(name = "잔여 좌석 수")
        private final int           remainingSeats;
        @JsonProperty("total_seats")
        @Schema(name = "전체 좌석 수")
        private final int           totalSeats;
        @JsonProperty("created_at")
        @Schema(name = "생성 일시")
        private final LocalDateTime createdAt;
        @JsonProperty("updated_at")
        @Schema(name = "수정 일시")
        private final LocalDateTime updatedAt;

        @QueryProjection
        @JsonCreator
        public PerformanceListResponse(@JsonProperty("id") final Long id,
                                       @JsonProperty("name") final String name,
                                       @JsonProperty("venue") final String venue,
                                       @JsonProperty("start_time") final LocalDateTime startTime,
                                       @JsonProperty("end_time") final LocalDateTime endTime,
                                       @JsonProperty("remaining_seats") final int remainingSeats,
                                       @JsonProperty("total_seats") final int totalSeats,
                                       @JsonProperty("created_at") final LocalDateTime createdAt,
                                       @JsonProperty("updated_at") final LocalDateTime updatedAt) {
            this.id = id;
            this.name = name;
            this.venue = venue;
            this.startTime = startTime;
            this.endTime = endTime;
            this.remainingSeats = remainingSeats;
            this.totalSeats = totalSeats;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public static PerformanceListResponse from(final Performance performance) {
            return new PerformanceListResponse(performance.getId(),
                                               performance.getName(),
                                               performance.getVenue(),
                                               performance.getStartTime(),
                                               performance.getEndTime(),
                                               (int) performance.getSeats()
                                                                .stream()
                                                                .filter(s -> s.getStatus() == AVAILABLE)
                                                                .count(),
                                               performance.getSeats().size(),
                                               performance.getCreatedAt(),
                                               performance.getUpdatedAt());
        }

    }

    @Getter
    @Schema(name = "공연 상세 정보 응답 DTO")
    public static class PerformanceDetailResponse {

        @Schema(name = "공연 ID")
        private final Long          id;
        @Schema(name = "공연 이름")
        private final String        name;
        @Schema(name = "공연 장소")
        private final String        venue;
        @Schema(name = "공연 정보")
        private final String        info;
        @JsonProperty("start_time")
        @Schema(name = "공연 시작 시간")
        private final LocalDateTime startTime;
        @JsonProperty("end_time")
        @Schema(name = "공연 종료 시간")
        private final LocalDateTime endTime;
        @JsonProperty("remaining_seats")
        @Schema(name = "잔여 좌석 수")
        private final int           remainingSeats;
        @JsonProperty("total_seats")
        @Schema(name = "전체 좌석 수")
        private final int           totalSeats;
        @JsonProperty("created_at")
        @Schema(name = "생성 일시")
        private final LocalDateTime createdAt;
        @JsonProperty("updated_at")
        @Schema(name = "수정 일시")
        private final LocalDateTime updatedAt;

        @QueryProjection
        public PerformanceDetailResponse(@JsonProperty("id") final Long id,
                                         @JsonProperty("name") final String name,
                                         @JsonProperty("venue") final String venue,
                                         @JsonProperty("info") final String info,
                                         @JsonProperty("start_time") final LocalDateTime startTime,
                                         @JsonProperty("end_time") final LocalDateTime endTime,
                                         @JsonProperty("remaining_seats") final int remainingSeats,
                                         @JsonProperty("total_seats") final int totalSeats,
                                         @JsonProperty("created_at") final LocalDateTime createdAt,
                                         @JsonProperty("updated_at") final LocalDateTime updatedAt) {
            this.id = id;
            this.name = name;
            this.venue = venue;
            this.info = info;
            this.startTime = startTime;
            this.endTime = endTime;
            this.remainingSeats = remainingSeats;
            this.totalSeats = totalSeats;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public static PerformanceDetailResponse from(final Performance performance) {
            return new PerformanceDetailResponse(performance.getId(),
                                                 performance.getName(),
                                                 performance.getVenue(),
                                                 performance.getInfo(),
                                                 performance.getStartTime(),
                                                 performance.getEndTime(),
                                                 (int) performance.getSeats()
                                                                  .stream()
                                                                  .filter(s -> s.getStatus() == AVAILABLE)
                                                                  .count(),
                                                 performance.getSeats().size(),
                                                 performance.getCreatedAt(),
                                                 performance.getUpdatedAt());
        }

    }

}
