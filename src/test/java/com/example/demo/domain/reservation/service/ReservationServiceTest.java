package com.example.demo.domain.reservation.service;

import static com.example.demo.common.response.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.example.demo.common.response.ErrorCode.RESERVATION_NOT_FOUND;
import static com.example.demo.common.response.ErrorCode.SEAT_NOT_FOUND;
import static com.example.demo.common.util.TestUtils.createAccount;
import static com.example.demo.common.util.TestUtils.createPerformance;
import static com.example.demo.common.util.TestUtils.createReservation;
import static com.example.demo.common.util.TestUtils.createReservationInfoResponses;
import static com.example.demo.common.util.TestUtils.createSeat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.common.error.BusinessException;
import com.example.demo.domain.account.dao.AccountRepository;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.performance.dao.SeatRepository;
import com.example.demo.domain.performance.model.Performance;
import com.example.demo.domain.performance.model.Seat;
import com.example.demo.domain.reservation.dao.ReservationRepository;
import com.example.demo.domain.reservation.dto.ReservationRequest.ReservationCreateRequest;
import com.example.demo.domain.reservation.dto.ReservationResponse.ReservationInfoResponse;
import com.example.demo.domain.reservation.model.Reservation;
import com.example.demo.domain.reservation.model.ReservationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * PackageName : com.example.demo.domain.reservation.service
 * FileName    : ReservationServiceTest
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : ReservationService 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    ReservationServiceImpl reservationService;
    @Mock
    ReservationRepository  reservationRepository;
    @Mock
    AccountRepository      accountRepository;
    @Mock
    SeatRepository         seatRepository;

    @Nested
    @DisplayName("reserveSeat() 테스트")
    class ReserveSeatTests {

        @RepeatedTest(10)
        @DisplayName("좌석 예약")
        void reserveSeat() {
            // given
            Account account = createAccount();
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());
            Performance performance = createPerformance();
            ReflectionTestUtils.setField(performance, "id", 1L);
            Seat seat = createSeat(performance);
            ReflectionTestUtils.setField(seat, "id", 1L);
            Reservation              reservation = createReservation(account, seat);
            ReservationCreateRequest request     = new ReservationCreateRequest(seat.getId());

            when(accountRepository.findByIdAndStatus(eq(account.getId()), any())).thenReturn(Optional.of(account));
            when(seatRepository.findById(eq(seat.getId()))).thenReturn(Optional.of(seat));
            when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

            // when
            reservationService.reserveSeat(account.getId(), request);

            // then
            verify(accountRepository, times(1)).findByIdAndStatus(eq(account.getId()), any());
            verify(seatRepository, times(1)).findById(eq(seat.getId()));
            verify(reservationRepository, times(1)).save(any(Reservation.class));
        }

        @RepeatedTest(10)
        @DisplayName("좌석 예약 시도, 계정이 존재하지 않음")
        void reserveSeat_accountNotFound() {
            // given
            UUID                     accountId = UUID.randomUUID();
            ReservationCreateRequest request   = new ReservationCreateRequest(1L);

            when(accountRepository.findByIdAndStatus(eq(accountId), any())).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationService.reserveSeat(accountId, request),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 ACCOUNT_NOT_FOUND여야 합니다."));

            verify(accountRepository, times(1)).findByIdAndStatus(eq(accountId), any());
            verify(seatRepository, never()).findById(any());
            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @RepeatedTest(10)
        @DisplayName("좌석 예약 시도, 좌석이 존재하지 않음")
        void reserveSeat_seatNotFound() {
            // given
            Account account = createAccount();
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());
            ReservationCreateRequest request = new ReservationCreateRequest(1L);

            when(accountRepository.findByIdAndStatus(eq(account.getId()), any())).thenReturn(Optional.of(account));
            when(seatRepository.findById(eq(request.getSeatId()))).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationService.reserveSeat(account.getId(), request),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(SEAT_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 SEAT_NOT_FOUND여야 합니다."));

            verify(accountRepository, times(1)).findByIdAndStatus(eq(account.getId()), any());
            verify(seatRepository, times(1)).findById(eq(request.getSeatId()));
            verify(reservationRepository, never()).save(any(Reservation.class));
        }

    }

    @Nested
    @DisplayName("cancelReservation() 테스트")
    class CancelReservationTests {

        @RepeatedTest(10)
        @DisplayName("예약 취소")
        void cancelReservation() {
            // given
            Account account = createAccount();
            ReflectionTestUtils.setField(account, "id", UUID.randomUUID());
            Performance performance = createPerformance();
            ReflectionTestUtils.setField(performance, "id", 1L);
            Seat seat = createSeat(performance);
            ReflectionTestUtils.setField(seat, "id", 1L);
            Reservation   reservation   = createReservation(account, seat);
            UUID          accountId     = account.getId();
            Long          seatId        = seat.getId();
            ReservationId reservationId = new ReservationId(accountId, seatId);

            when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));

            // when
            reservationService.cancelReservation(accountId, seatId);

            // then
            verify(reservationRepository, times(1)).findById(eq(reservationId));
            verify(reservationRepository, times(1)).delete(eq(reservation));
        }

        @RepeatedTest(10)
        @DisplayName("예약 취소 시도, 예약이 존재하지 않음")
        void cancelReservation_reservationNotFound() {
            // given
            UUID          accountId     = UUID.randomUUID();
            Long          seatId        = 1L;
            ReservationId reservationId = new ReservationId(accountId, seatId);

            when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> reservationService.cancelReservation(accountId, seatId),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(RESERVATION_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 RESERVATION_NOT_FOUND여야 합니다."));

            verify(reservationRepository, times(1)).findById(eq(reservationId));
            verify(reservationRepository, never()).delete(any(Reservation.class));
        }

    }

    @Nested
    @DisplayName("getMyReservations() 테스트")
    class GetMyReservationsTests {

        @RepeatedTest(10)
        @DisplayName("내 예약 목록 조회")
        void getMyReservations() {
            // given
            UUID                          accountId       = UUID.randomUUID();
            PageRequest                   pageable        = PageRequest.of(0, 10);
            List<ReservationInfoResponse> reservationList = createReservationInfoResponses(10);
            Page<ReservationInfoResponse> reservationPage = new PageImpl<>(
                    reservationList, pageable, reservationList.size()
            );

            when(reservationRepository.getMyReservations(eq(accountId), eq(pageable))).thenReturn(reservationPage);

            // when
            Page<ReservationInfoResponse> result = reservationService.getMyReservations(accountId, pageable);

            // then
            assertEquals(10, result.getTotalElements(), "총 예약 수는 10이어야 합니다.");
            assertEquals(reservationList.size(), result.getContent().size(), "페이지 내 예약 수는 10이어야 합니다.");
            verify(reservationRepository, times(1)).getMyReservations(eq(accountId), eq(pageable));
        }

        @RepeatedTest(10)
        @DisplayName("내 예약 목록 조회, 예약이 없음")
        void getMyReservations_empty() {
            // given
            UUID                          accountId = UUID.randomUUID();
            PageRequest                   pageable  = PageRequest.of(0, 10);
            Page<ReservationInfoResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(reservationRepository.getMyReservations(eq(accountId), eq(pageable))).thenReturn(emptyPage);

            // when
            Page<ReservationInfoResponse> result = reservationService.getMyReservations(accountId, pageable);

            // then
            assertEquals(0, result.getTotalElements(), "총 예약 수는 0이어야 합니다.");
            assertEquals(0, result.getContent().size(), "페이지 내 예약 수는 0이어야 합니다.");
            verify(reservationRepository, times(1)).getMyReservations(eq(accountId), eq(pageable));
        }

    }

}
