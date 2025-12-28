package com.example.demo.domain.reservation.service;

import static com.example.demo.common.util.TestUtils.createAccounts;
import static com.example.demo.common.util.TestUtils.createPerformance;
import static com.example.demo.common.util.TestUtils.createSeat;
import static com.example.demo.domain.account.model.AccountStatus.ACTIVE;
import static com.example.demo.domain.performance.model.SeatStatus.TEMPORARY_RESERVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import com.example.demo.domain.account.dao.AccountRepository;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.performance.dao.PerformanceRepository;
import com.example.demo.domain.performance.dao.SeatRepository;
import com.example.demo.domain.performance.model.Seat;
import com.example.demo.domain.reservation.dao.ReservationRepository;
import com.example.demo.domain.reservation.dto.ReservationRequest.ReservationCreateRequest;
import com.example.demo.domain.reservation.model.Reservation;
import com.example.demo.domain.reservation.model.ReservationId;
import com.example.demo.infra.redis.config.TestRedisConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * PackageName : com.example.demo.domain.reservation.service
 * FileName    : ReservationServiceConcurrencyTest
 * Author      : oldolgol331
 * Date        : 25. 12. 27.
 * Description : ReservationService 동시성 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 27.   oldolgol331          Initial creation
 */
@Tag("concurrency")
@Import(TestRedisConfig.class)
@SpringBootTest
@SqlGroup({@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:/sql/truncate_tables.sql"),
           @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:/sql/truncate_tables.sql")})
@TestMethodOrder(OrderAnnotation.class)
@Slf4j
class ReservationServiceConcurrencyTest {

    private static final int THREAD_COUNT = Math.max(100, Runtime.getRuntime().availableProcessors());

    @Autowired
    TransactionTemplate   transactionTemplate;
    @Autowired
    ReservationService    reservationService;
    @Autowired
    AccountRepository     accountRepository;
    @Autowired
    PerformanceRepository performanceRepository;
    @Autowired
    SeatRepository        seatRepository;
    @Autowired
    ReservationRepository reservationRepository;

    @Nested
    @DisplayName("reserveSeat() 테스트")
    class ReserveSeatTests {

        @Test
        @DisplayName("[Redisson 락 동작 테스트] 동시에 예약 시 락 획득에 성공한 클라이언트만 예약되어야 함")
        void reserveSeat() throws InterruptedException {
            // given
            List<Account> accounts = createAccounts(THREAD_COUNT);
            accounts.forEach(
                    account -> {
                        account.setNickname(
                                account.getNickname() + "_" + UUID.randomUUID().toString().substring(0, 5)
                        );
                        account.setStatus(ACTIVE);
                    }
            );
            List<UUID> accountIds = transactionTemplate.execute(
                    status -> accountRepository.saveAll(accounts)
                                               .stream()
                                               .map(Account::getId)
                                               .toList()
            );
            Long seatId = transactionTemplate.execute(
                    status -> seatRepository.save(createSeat(performanceRepository.save(createPerformance()))).getId()
            );

            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
            CountDownLatch  startLatch      = new CountDownLatch(1);
            CountDownLatch  endLatch        = new CountDownLatch(THREAD_COUNT);

            AtomicInteger successThreadIndex = new AtomicInteger();

            // when
            for (int i = 1; i <= THREAD_COUNT; i++) {
                int threadIndex = i;
                executorService.execute(() -> {
                    try {
                        startLatch.await();

                        UUID                     accountId = accountIds.get(threadIndex - 1);
                        ReservationCreateRequest request   = new ReservationCreateRequest(seatId);

                        log.info("[Thread-{}] 예약 요청 시작, 계정 ID: {}", threadIndex, accountId);
                        reservationService.reserveSeat(accountId, request);

                        successThreadIndex.set(threadIndex);
                    } catch (Exception e) {
                        log.error("[Thread-{}] Error: ", threadIndex, e);
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            endLatch.await();
            executorService.shutdown();

            // then
            Seat        seat         = seatRepository.findById(seatId).orElseThrow();
            Reservation reservation  = null;
            int         successCount = 0;
            for (UUID accountId : accountIds) {
                Optional<Reservation> opReservation =
                        reservationRepository.findById(new ReservationId(accountId, seatId));
                if (opReservation.isPresent()) {
                    successCount++;
                    reservation = opReservation.get();
                }
            }

            log.info("Expected Success Index: {}", successThreadIndex);

            assertEquals(TEMPORARY_RESERVED, seat.getStatus(), "좌석 상태는 TEMPORARY_RESERVED여야 합니다.");
            assertEquals(1, successCount, "성공한 예약은 1개여야 합니다.");
            assertEquals(seatId, reservation.getSeatId(), "예약된 좌석 ID가 일치해야 합니다.");
        }

    }

}
