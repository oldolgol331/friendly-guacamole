package com.example.demo.domain.reservation.service;

import static com.example.demo.common.response.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.example.demo.common.response.ErrorCode.RESERVATION_NOT_FOUND;
import static com.example.demo.common.response.ErrorCode.SEAT_NOT_FOUND;
import static com.example.demo.domain.account.model.AccountStatus.ACTIVE;

import com.example.demo.common.error.BusinessException;
import com.example.demo.domain.account.dao.AccountRepository;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.reservation.dao.ReservationRepository;
import com.example.demo.domain.reservation.dto.ReservationRequest.ReservationCreateRequest;
import com.example.demo.domain.reservation.dto.ReservationResponse.ReservationInfoResponse;
import com.example.demo.domain.reservation.model.Reservation;
import com.example.demo.domain.reservation.model.ReservationId;
import com.example.demo.domain.performance.dao.SeatRepository;
import com.example.demo.domain.performance.model.Seat;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.example.demo.domain.reservation.service
 * FileName    : ReservationServiceImpl
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : 예약(Reservation) 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final AccountRepository     accountRepository;
    private final SeatRepository        seatRepository;

    /**
     * 계정과 좌석 정보를 사용해 예약을 합니다.
     *
     * @param accountId - 예약할 계정 ID
     * @param request   - 좌석 예약 요청 DTO
     */
    @Transactional
    @Override
    public void reserveSeat(final UUID accountId, final ReservationCreateRequest request) {
        Account account = accountRepository.findByIdAndStatus(accountId, ACTIVE)
                                           .orElseThrow(() -> new BusinessException(ACCOUNT_NOT_FOUND));

        Seat seat = seatRepository.findById(request.getSeatId())
                                  .orElseThrow(() -> new BusinessException(SEAT_NOT_FOUND));

        Reservation reservation = reservationRepository.save(Reservation.of(account, seat));
        // TODO: 결제 완료 -> 예약 확정 시 호출
        reservation.complete(LocalDateTime.now());  // HACK: 임시로 예약 확정 처리
    }

    /**
     * 예약된 좌석을 취소합니다.
     *
     * @param accountId - 에약한 계정 ID
     * @param seatId    - 예약된 좌석 ID
     */
    @Transactional
    @Override
    public void cancelReservation(final UUID accountId, final Long seatId) {
        Reservation reservation = reservationRepository.findById(new ReservationId(accountId, seatId))
                                                       .orElseThrow(() -> new BusinessException(RESERVATION_NOT_FOUND));
        reservation.cancel();
        reservationRepository.delete(reservation);
    }

    /**
     * 계정의 예약 목록을 조회합니다.
     *
     * @param accountId - 계정 ID
     * @param pageable  - 페이징 객체
     * @return 계정의 예약 페이징 목록 응답 DTO
     */
    @Override
    public Page<ReservationInfoResponse> getMyReservations(final UUID accountId, final Pageable pageable) {
        return reservationRepository.getMyReservations(accountId, pageable);
    }

}
