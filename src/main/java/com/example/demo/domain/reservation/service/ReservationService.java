package com.example.demo.domain.reservation.service;

import com.example.demo.domain.reservation.dto.ReservationRequest.ReservationCreateRequest;
import com.example.demo.domain.reservation.dto.ReservationResponse.ReservationInfoResponse;
import com.example.demo.domain.reservation.model.Reservation;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PackageName : com.example.demo.domain.reservation.service
 * FileName    : ReservationService
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : 예약(Reservation) 서비스 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
public interface ReservationService {

    void reserveSeat(UUID accountId, ReservationCreateRequest request);

    void cancelReservation(UUID accountId, Long seatId);

    void cancelReservation(Reservation reservation);

    Page<ReservationInfoResponse> getMyReservations(UUID accountId, Pageable pageable);

    Reservation findReservationById(UUID accountId, Long seatId);

}
