package com.example.demo.domain.reservation.scheduler;

import static com.example.demo.domain.reservation.model.ReservationStatus.PENDING_PAYMENT;

import com.example.demo.domain.reservation.dao.ReservationRepository;
import com.example.demo.domain.reservation.model.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.example.demo.domain.reservation.scheduler
 * FileName    : ReservationScheduler
 * Author      : oldolgol331
 * Date        : 25. 12. 25.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 25.   oldolgol331          Initial creation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cancelExpiredReservations(){
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpiredAtBefore(
                PENDING_PAYMENT, now
        );

        if (expiredReservations.isEmpty()) return;

        log.info("만료된 예약 정리 시작: {}건", expiredReservations.size());

        for (Reservation reservation : expiredReservations)
            try {
                reservation.cancel();
            } catch (Exception e) {
                log.error("예약 취소 처리 중 오류 발생 - reservationId: {}", reservation.getReservationId(), e);
            }

        log.info("만료된 예약 정리 완료");
    }

}
