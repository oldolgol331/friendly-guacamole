package com.example.demo.domain.performance.dao;

import static com.example.demo.common.util.TestUtils.createPerformance;
import static com.example.demo.common.util.TestUtils.createSeat;
import static com.example.demo.domain.performance.model.SeatStatus.AVAILABLE;
import static com.example.demo.domain.performance.model.SeatStatus.TEMPORARY_RESERVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import autoparams.AutoSource;
import autoparams.Repeat;
import com.example.demo.common.config.EnableJpaAuditingConfig;
import com.example.demo.common.config.P6SpyConfig;
import com.example.demo.common.config.QuerydslConfig;
import com.example.demo.domain.performance.model.Performance;
import com.example.demo.domain.performance.model.Seat;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

/**
 * PackageName : com.example.demo.domain.performance.dao
 * FileName    : SeatRepositoryTest
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : SeatRepository 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@DataJpaTest
@Import({EnableJpaAuditingConfig.class, P6SpyConfig.class, QuerydslConfig.class})
class SeatRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    SeatRepository    seatRepository;

    @Nested
    @DisplayName("save() 테스트")
    class SaveTests {

        @RepeatedTest(10)
        @DisplayName("Seat 엔티티 저장")
        void save() {
            // given
            var  performance = em.persistAndFlush(createPerformance());
            Seat seat        = createSeat(performance);

            // when
            Long id = seatRepository.save(seat).getId();
            em.flush();

            // then
            Seat savedSeat = em.find(Seat.class, id);

            assertNotNull(savedSeat, "savedSeat는 null이 아니어야 합니다.");
            assertEquals(seat.getSeatCode(), savedSeat.getSeatCode(), "seatCode는 같아야 합니다.");
            assertEquals(seat.getPrice(), savedSeat.getPrice(), "price는 같아야 합니다.");
            assertEquals(seat.getStatus(), savedSeat.getStatus(), "status는 같아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 Seat 엔티티 단 건 조회")
        void findById() {
            // given
            var  performance = em.persistAndFlush(createPerformance());
            Seat seat        = em.persistAndFlush(createSeat(performance));
            Long id          = seat.getId();

            // when
            Seat findSeat = seatRepository.findById(id).get();

            // then
            assertNotNull(findSeat, "findSeat는 null이 아니어야 합니다.");
            assertEquals(seat.getSeatCode(), findSeat.getSeatCode(), "seatCode는 같아야 합니다.");
            assertEquals(seat.getPrice(), findSeat.getPrice(), "price는 같아야 합니다.");
            assertEquals(seat.getStatus(), findSeat.getStatus(), "status는 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id로 Seat 엔티티 단 건 조회 시도")
        void findById_unknownId(final Long unknownId) {
            // when
            Optional<Seat> opSeat = seatRepository.findById(unknownId);

            // then
            assertFalse(opSeat.isPresent(), "조회된 Seat 엔티티가 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("existsByPerformanceIdAndStatusNot() 테스트")
    class ExistsByPerformanceIdAndStatusNotTests {

        @RepeatedTest(10)
        @DisplayName("공연 ID와 상태로 예약된 좌석 존재 여부 확인")
        void existsByPerformanceIdAndStatusNot() {
            // given
            Performance performance = em.persistAndFlush(createPerformance());
            Seat        seat        = createSeat(performance);
            seat.setStatus(TEMPORARY_RESERVED);
            em.persistAndFlush(seat);

            // when
            boolean exists = seatRepository.existsByPerformanceIdAndStatusNot(performance.getId(), AVAILABLE);

            // then
            assertTrue(exists, "예약된 좌석이 존재해야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("공연 ID와 상태로 예약된 좌석 존재 여부 확인 - 존재하지 않는 경우")
        void existsByPerformanceIdAndStatusNot_notExists() {
            // given
            var performance = em.persistAndFlush(createPerformance());
            var seat        = createSeat(performance);
            seat.setStatus(AVAILABLE);
            em.persistAndFlush(seat);

            // when
            boolean exists = seatRepository.existsByPerformanceIdAndStatusNot(performance.getId(), AVAILABLE);

            // then
            assertFalse(exists, "예약된 좌석이 존재하지 않아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 공연 ID로 예약된 좌석 존재 여부 확인")
        void existsByPerformanceIdAndStatusNot_unknownPerformanceId(final Long unknownPerformanceId) {
            // when
            boolean exists = seatRepository.existsByPerformanceIdAndStatusNot(unknownPerformanceId, AVAILABLE);

            // then
            assertFalse(exists, "예약된 좌석이 존재하지 않아야 합니다.");
        }

    }

}