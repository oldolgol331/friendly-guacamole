package com.example.demo.domain.performance.dto;

import static lombok.AccessLevel.PRIVATE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.domain.performance.dto
 * FileName    : PerformanceRequest
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 공연 도메인 요청 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(name = "공연 도메인 요청 DTO")
public abstract class PerformanceRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "공연 생성 요청 DTO")
    public static class PerformanceCreateRequest {

        @NotBlank(message = "공연 이름은 필수입니다.")
        @Schema(name = "공연 이름")
        private String name;

        @NotBlank(message = "공연 장소는 필수입니다.")
        @Schema(name = "공연 장소")
        private String venue;

        @Schema(name = "공연 정보")
        private String info;

        @NotNull(message = "공연 시작 시간은 필수입니다.")
        @Schema(name = "공연 시작 시간")
        private LocalDateTime startTime;

        @NotNull(message = "공연 종료 시간은 필수입니다.")
        @Schema(name = "공연 종료 시간")
        private LocalDateTime endTime;

        @Min(value = 1, message = "좌석 수는 최소 1개 이상이어야 합니다.")
        @Schema(name = "공연 생성 시 자동으로 만들 좌석 수")
        private int totalSeats;

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        @Schema(name = "공연 가격")
        private int price;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "공연 수정 요청 DTO")
    public static class PerformanceUpdateRequest {

        @NotBlank(message = "공연 이름은 필수입니다.")
        @Schema(name = "공연 이름")
        private String name;

        @NotBlank(message = "공연 장소는 필수입니다.")
        @Schema(name = "공연 장소")
        private String venue;

        @Schema(name = "공연 정보")
        private String info;

        @NotNull(message = "공연 시작 시간은 필수입니다.")
        @Schema(name = "공연 시작 시간")
        private LocalDateTime startTime;

        @NotNull(message = "공연 종료 시간은 필수입니다.")
        @Schema(name = "공연 종료 시간")
        private LocalDateTime endTime;

    }

}
