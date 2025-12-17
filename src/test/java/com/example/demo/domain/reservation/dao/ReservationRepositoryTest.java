package com.example.demo.domain.reservation.dao;

import static com.example.demo.common.util.TestUtils.createAccount;
import static com.example.demo.common.util.TestUtils.createPerformance;
import static com.example.demo.common.util.TestUtils.createReservation;
import static com.example.demo.common.util.TestUtils.createSeat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import autoparams.AutoSource;
import autoparams.Repeat;
import com.example.demo.common.config.EnableJpaAuditingConfig;
import com.example.demo.common.config.P6SpyConfig;
import com.example.demo.common.config.QuerydslConfig;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.performance.model.Performance;
import com.example.demo.domain.performance.model.Seat;
import com.example.demo.domain.reservation.model.Reservation;
import com.example.demo.domain.reservation.model.ReservationId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

/**
 * PackageName : com.example.demo.domain.reservation.dao
 * FileName    : ReservationRepositoryTest
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : ReservationRepository 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@DataJpaTest
@Import({EnableJpaAuditingConfig.class, P6SpyConfig.class, QuerydslConfig.class})
class ReservationRepositoryTest {

    @Autowired
    TestEntityManager     em;
    @Autowired
    ReservationRepository reservationRepository;

    @Nested
    @DisplayName("save() 테스트")
    class SaveTests {

        @RepeatedTest(10)
        @DisplayName("Reservation 엔티티 저장")
        void save() {
            // given
            Account     account     = em.persistAndFlush(createAccount());
            Performance performance = em.persistAndFlush(createPerformance());
            Seat        seat        = em.persistAndFlush(createSeat(performance));
            Reservation reservation = createReservation(account, seat);

            // when
            reservationRepository.save(reservation);
            ReservationId id = new ReservationId(account.getId(), seat.getId());
            em.flush();

            // then
            Reservation savedReservation = em.find(Reservation.class, id);

            assertNotNull(savedReservation, "savedReservation는 null이 아니어야 합니다.");
            assertEquals(reservation.getAccountId(), savedReservation.getAccountId(), "accountId는 같아야 합니다.");
            assertEquals(reservation.getSeatId(), savedReservation.getSeatId(), "seatId는 같아야 합니다.");
            assertEquals(reservation.getSeat().getId(), savedReservation.getSeat().getId(), "seatId는 같아야 합니다.");
            assertEquals(reservation.getAccount().getId(), savedReservation.getAccount().getId(),
                         "accountId는 같아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 Reservation 엔티티 단 건 조회")
        void findById() {
            // given
            Account       account     = em.persistAndFlush(createAccount());
            Performance   performance = em.persistAndFlush(createPerformance());
            Seat          seat        = em.persistAndFlush(createSeat(performance));
            Reservation   reservation = em.persistAndFlush(createReservation(account, seat));
            ReservationId id          = new ReservationId(account.getId(), seat.getId());

            // when
            Reservation findReservation = reservationRepository.findById(id).get();

            // then
            assertNotNull(findReservation, "findReservation은 null이 아니어야 합니다.");
            assertEquals(reservation.getAccountId(), findReservation.getAccountId(), "accountId는 같아야 합니다.");
            assertEquals(reservation.getSeatId(), findReservation.getSeatId(), "seatId는 같아야 합니다.");
            assertEquals(reservation.getSeat().getId(), findReservation.getSeat().getId(), "seatId는 같아야 합니다.");
            assertEquals(reservation.getAccount().getId(), findReservation.getAccount().getId(), "accountId는 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id로 Reservation 엔티티 단 건 조회 시도")
        void findById_unknownId(final UUID unknownAccountId, final Long unknownSeatId) {
            // given
            ReservationId unknownId = new ReservationId(unknownAccountId, unknownSeatId);

            // when
            Optional<Reservation> opReservation = reservationRepository.findById(unknownId);

            // then
            assertFalse(opReservation.isPresent(), "조회된 Reservation 엔티티가 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("deleteById() 테스트")
    class DeleteByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 Reservation 엔티티 삭제")
        void deleteById() {
            // given
            Account       account     = em.persistAndFlush(createAccount());
            Performance   performance = em.persistAndFlush(createPerformance());
            Seat          seat        = em.persistAndFlush(createSeat(performance));
            Reservation   reservation = em.persistAndFlush(createReservation(account, seat));
            ReservationId id          = new ReservationId(reservation.getAccountId(), reservation.getSeatId());

            // when
            reservationRepository.deleteById(id);
            em.flush();

            // then
            Reservation deletedReservation = em.find(Reservation.class, id);

            assertTrue(deletedReservation == null, "deletedReservation는 null이어야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id로 Reservation 엔티티 삭제 시도")
        void deleteById_unknownId(final UUID unknownAccountId, final Long unknownSeatId) {
            // given
            ReservationId unknownId = new ReservationId(unknownAccountId, unknownSeatId);

            // when & then
            reservationRepository.deleteById(unknownId);
            em.flush();
        }

    }

}
