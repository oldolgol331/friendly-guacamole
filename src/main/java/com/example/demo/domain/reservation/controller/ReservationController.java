package com.example.demo.domain.reservation.controller;

import static com.example.demo.common.response.SuccessCode.RESERVATION_CANCEL_SUCCESS;
import static com.example.demo.common.response.SuccessCode.RESERVATION_CREATE_SUCCESS;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.SuccessCode;
import com.example.demo.common.security.model.CustomUserDetails;
import com.example.demo.domain.reservation.dto.ReservationRequest.ReservationCreateRequest;
import com.example.demo.domain.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.example.demo.domain.reservation.controller
 * FileName    : ReservationController
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : 예약(Reservation) 컨트롤러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "예약 API", description = "공연 좌석 예약, 예약 취소 API를 제공합니다.")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "좌석 예약", description = "공연 좌석을 예약합니다.")
    public ResponseEntity<ApiResponse<Void>> reserveSeat(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @Valid @RequestBody final ReservationCreateRequest request
    ) {
        reservationService.reserveSeat(userDetails.getId(), request);
        final SuccessCode successCode = RESERVATION_CREATE_SUCCESS;
        return ResponseEntity.status(successCode.getStatus()).body(ApiResponse.success(successCode));
    }

    @DeleteMapping("/{seatId}")
    @Operation(summary = "예약 취소", description = "예약된 좌석을 취소합니다.")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @PathVariable("seatId") @Min(1) final Long id
    ) {
        reservationService.cancelReservation(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(RESERVATION_CANCEL_SUCCESS));
    }

}
