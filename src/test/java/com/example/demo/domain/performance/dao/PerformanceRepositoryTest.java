package com.example.demo.domain.performance.dao;

import static com.example.demo.common.util.TestUtils.createPerformance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import autoparams.AutoSource;
import autoparams.Repeat;
import com.example.demo.common.config.EnableJpaAuditingConfig;
import com.example.demo.common.config.P6SpyConfig;
import com.example.demo.common.config.QuerydslConfig;
import com.example.demo.domain.performance.model.Performance;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
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
 * FileName    : PerformanceRepositoryTest
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : PerformanceRepository 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@DataJpaTest
@Import({EnableJpaAuditingConfig.class, P6SpyConfig.class, QuerydslConfig.class})
class PerformanceRepositoryTest {

    @Autowired
    TestEntityManager     em;
    @Autowired
    PerformanceRepository performanceRepository;

    @Nested
    @DisplayName("save() 테스트")
    class SaveTests {

        @RepeatedTest(10)
        @DisplayName("Performance 엔티티 저장")
        void save() {
            // given
            Performance performance = createPerformance();

            // when
            Long id = performanceRepository.save(performance).getId();
            em.flush();

            // then
            Performance savedPerformance = em.find(Performance.class, id);

            assertNotNull(savedPerformance, "savedPerformance는 null이 아니어야 합니다.");
            assertEquals(performance.getName(), savedPerformance.getName(), "name은 같아야 합니다.");
            assertEquals(performance.getVenue(), savedPerformance.getVenue(), "venue은 같아야 합니다.");
            assertEquals(performance.getInfo(), savedPerformance.getInfo(), "info는 같아야 합니다.");
            assertEquals(performance.getStartTime(), savedPerformance.getStartTime(), "startTime은 같아야 합니다.");
            assertEquals(performance.getEndTime(), savedPerformance.getEndTime(), "endTime은 같아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 Performance 엔티티 단 건 조회")
        void findById() {
            // given
            Performance performance = em.persistAndFlush(createPerformance());
            Long        id          = performance.getId();

            // when
            Performance findPerformance = performanceRepository.findById(id).get();

            // then
            assertNotNull(findPerformance, "findPerformance는 null이 아니어야 합니다.");
            assertEquals(performance.getName(), findPerformance.getName(), "name은 같아야 합니다.");
            assertEquals(performance.getVenue(), findPerformance.getVenue(), "venue은 같아야 합니다.");
            assertEquals(performance.getInfo(), findPerformance.getInfo(), "info는 같아야 합니다.");
            assertEquals(performance.getStartTime(), findPerformance.getStartTime(), "startTime은 같아야 합니다.");
            assertEquals(performance.getEndTime(), findPerformance.getEndTime(), "endTime은 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id로 Performance 엔티티 단 건 조회 시도")
        void findById_unknownId(@Min(1) @Max(Long.MAX_VALUE) final long unknownId) {
            // when
            Optional<Performance> opPerformance = performanceRepository.findById(unknownId);

            // then
            assertFalse(opPerformance.isPresent(), "조회된 Performance는 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("엔티티 필드 업데이트 테스트")
    class DirtyCheckingTests {

        @RepeatedTest(10)
        @DisplayName("Performance 엔티티 필드 업데이트")
        void update() {
            // given
            Performance   performance     = em.persistAndFlush(createPerformance());
            Long          id              = performance.getId();
            String        beforeName      = performance.getName();
            String        beforeVenue     = performance.getVenue();
            String        beforeInfo      = performance.getInfo();
            LocalDateTime beforeStartTime = performance.getStartTime();
            LocalDateTime beforeEndTime   = performance.getEndTime();

            // when
            Performance findPerformance = performanceRepository.findById(id).get();
            findPerformance.setName("updated" + findPerformance.getName());
            findPerformance.setVenue("updated" + findPerformance.getVenue());
            findPerformance.setInfo("updated" + findPerformance.getInfo());
            findPerformance.setPerformanceTime(findPerformance.getStartTime().plusDays(1),
                                               findPerformance.getEndTime().plusDays(1));

            // then
            Performance updatedPerformance = em.find(Performance.class, id);

            assertNotNull(updatedPerformance, "updatedPerformance는 null이 아니어야 합니다.");
            assertEquals(findPerformance.getName(), updatedPerformance.getName(), "name은 업데이트된 값과 같아야 합니다.");
            assertNotEquals(beforeName, updatedPerformance.getName(), "name은 업데이트 이전 값과 달야 합니다.");
            assertEquals(findPerformance.getVenue(), updatedPerformance.getVenue(), "venue은 업데이트된 값과 같아야 합니다.");
            assertNotEquals(beforeVenue, updatedPerformance.getVenue(), "venue은 업데이트 이전 값과 달라야 합니다.");
            assertEquals(findPerformance.getInfo(), updatedPerformance.getInfo(), "info은 업데이트된 값과 같아야 합니다.");
            assertNotEquals(beforeInfo, updatedPerformance.getInfo(), "info은 업데이트 이전 값과 달라야 합니다.");
            assertEquals(findPerformance.getStartTime(), updatedPerformance.getStartTime(),
                         "startTime은 업데이트된 값과 같아야 합니다.");
            assertNotEquals(beforeStartTime, updatedPerformance.getStartTime(), "startTime은 업데이트 이전 값과 달라야 합니다.");
            assertEquals(findPerformance.getEndTime(), updatedPerformance.getEndTime(), "endTime은 업데이트된 값과 같아야 합니다.");
            assertNotEquals(beforeEndTime, updatedPerformance.getEndTime(), "endTime은 업데이트 이전 값과 달라야 합니다.");
        }

    }

    @Nested
    @DisplayName("deleteById() 테스트")
    class DeleteByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 Performance 엔티티 삭제")
        void deleteById() {
            // given
            Performance performance = em.persistAndFlush(createPerformance());
            Long        id          = performance.getId();

            // when
            performanceRepository.deleteById(id);

            // then
            Performance deletedPerformance = em.find(Performance.class, id);

            assertNull(deletedPerformance, "deletedPerformance는 null이어야 합니다.");
        }

    }

}
