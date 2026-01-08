package com.example.demo.domain.performance.controller;

import static com.example.demo.common.response.SuccessCode.SEAT_LIST_READ_SUCCESS;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.domain.performance.dto.SeatResponse.SeatListResponse;
import com.example.demo.domain.performance.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.example.demo.domain.performance.controller
 * FileName    : SeatController
 * Author      : oldolgol331
 * Date        : 26. 1. 8.
 * Description : 좌석(Seat) 컨트롤러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 1. 8.     oldolgol331          Initial creation
 */
@RestController
@RequestMapping("/api/v1/performances/{performanceId}/seats")
@RequiredArgsConstructor
@Tag(name = "좌석 API", description = "좌석 목록 조회 API를 제공합니다.")
public class SeatController {

    private final SeatService seatService;

    @GetMapping
    @Operation(summary = "좌석 목록 조회", description = "공연의 좌석 정보 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<SeatListResponse>>> getSeats(@PathVariable("performanceId") final Long id) {
        return ResponseEntity.ok(ApiResponse.success(SEAT_LIST_READ_SUCCESS, seatService.getAllSeatsByPerformance(id)));
    }

}
