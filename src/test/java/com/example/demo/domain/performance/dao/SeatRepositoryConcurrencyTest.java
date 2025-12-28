package com.example.demo.domain.performance.dao;

import static com.example.demo.common.util.TestUtils.createPerformance;
import static com.example.demo.common.util.TestUtils.createSeat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import com.example.demo.domain.performance.model.Performance;
import com.example.demo.domain.performance.model.Seat;
import com.example.demo.infra.redis.config.TestRedisConfig;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * PackageName : com.example.demo.domain.performance.dao
 * FileName    : SeatRepositoryConcurrencyTest
 * Author      : oldolgol331
 * Date        : 25. 12. 26.
 * Description : SeatRepository 동시성 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 26.   oldolgol331          Initial creation
 */
@Tag("concurrency")
@Import(TestRedisConfig.class)
@SpringBootTest
@SqlGroup({@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:/sql/truncate_tables.sql"),
           @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:/sql/truncate_tables.sql")})
@TestMethodOrder(OrderAnnotation.class)
@Slf4j
class SeatRepositoryConcurrencyTest {

    private static final int THREAD_COUNT = Math.max(100, Runtime.getRuntime().availableProcessors());

    @Autowired
    TransactionTemplate   transactionTemplate;
    @Autowired
    PerformanceRepository performanceRepository;
    @Autowired
    SeatRepository        seatRepository;

    @Nested
    @DisplayName("findByIdWithLock() 테스트")
    class FindByIdWithLockTests {

        @Test
        @Order(1)
        @DisplayName("[비관적 락 동작 테스트] 동시에 조회 및 수정 시 마지막 커밋 데이터가 반영되어야 함")
        void findByIdWithLock() throws Exception {
            // given
            Long id = transactionTemplate.execute(status -> {
                Performance performance = performanceRepository.save(createPerformance());
                Seat        seat        = seatRepository.save(createSeat(performance));
                return seat.getId();
            });

            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
            CountDownLatch  startLatch      = new CountDownLatch(1);
            CountDownLatch  endLatch        = new CountDownLatch(THREAD_COUNT);

            AtomicInteger lastSuccessThreadIndex = new AtomicInteger();

            // when
            for (int i = 1; i <= THREAD_COUNT; i++) {
                int threadIndex = i;
                executorService.execute(() -> {
                    try {
                        startLatch.await(); // 메인 스레드 신호 대기 (최대한 동시에 스레드 경쟁을 위함)

                        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                            @Override
                            protected void doInTransactionWithoutResult(TransactionStatus status) {
                                Seat seat = seatRepository.findByIdWithLock(id).orElseThrow();

                                String newSeatCode = "Seat-" + threadIndex;
                                seat.setSeatCode(newSeatCode);
                                seatRepository.saveAndFlush(seat);

                                log.info("[Thread-{}] Updated to {}", threadIndex, newSeatCode);
                                lastSuccessThreadIndex.set(threadIndex);
                            }
                        });
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
            String finalSeatCode = transactionTemplate.execute(
                    status -> seatRepository.findById(id).orElseThrow().getSeatCode()
            );

            log.info("Expected Last Index: {}, Actual DB Value: {}", lastSuccessThreadIndex.get(), finalSeatCode);

            assertEquals("Seat-" + lastSuccessThreadIndex.get(), finalSeatCode);
        }

    }

}
