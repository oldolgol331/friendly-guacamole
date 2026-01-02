package com.example.demo.domain.performance.dao;

import static com.example.demo.common.util.TestUtils.FAKER;
import static com.example.demo.common.util.TestUtils.createPerformance;
import static com.example.demo.common.util.TestUtils.createPerformances;
import static com.example.demo.common.util.TestUtils.getDiffTime;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import autoparams.AutoSource;
import autoparams.Repeat;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceDetailResponse;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceListResponse;
import com.example.demo.domain.performance.model.Performance;
import com.example.demo.infra.mysql.common.AbstractMySQLIntegrationTest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * PackageName : com.example.demo.domain.performance.dao
 * FileName    : PerformanceRepositoryCustomTest
 * Author      : oldolgol331
 * Date        : 26. 1. 2.
 * Description : PerformanceRepositoryCustom 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 1. 2.     oldolgol331          Initial creation
 */
class PerformanceRepositoryCustomTest extends AbstractMySQLIntegrationTest {

    @Autowired
    PerformanceRepository performanceRepository;

    @AfterEach
    void tearDown() {
        performanceRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("getPerformance() 테스트")
    class GetPerformanceTests {

        @RepeatedTest(10)
        @DisplayName("id로 PerformanceDetailResponse DTO 단 건 조회")
        void getPerformance() {
            // given
            Performance performance = performanceRepository.save(createPerformance());
            Long        id          = performance.getId();

            // when
            PerformanceDetailResponse findPerformanceDetailResponse = performanceRepository.getPerformance(id).get();

            // then
            assertNotNull(findPerformanceDetailResponse, "findPerformanceDetailResponse는 null이 아니어야 합니다.");
            assertEquals(performance.getName(), findPerformanceDetailResponse.getName(), "name은 같아야 합니다.");
            assertEquals(performance.getVenue(), findPerformanceDetailResponse.getVenue(), "venue은 같아야 합니다.");
            assertEquals(performance.getInfo(), findPerformanceDetailResponse.getInfo(), "info는 같아야 합니다.");
            assertTrue(getDiffTime(performance.getStartTime(),
                                   findPerformanceDetailResponse.getStartTime(),
                                   SECONDS) <= 1,
                       "startTime는 1초 이내 차이여야 합니다.");
            assertTrue(getDiffTime(performance.getEndTime(),
                                   findPerformanceDetailResponse.getEndTime(),
                                   SECONDS) <= 1,
                       "endTime는 1초 이내 차이여야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id로 PerformanceDetailResponse DTO 단 건 조회 시도")
        void getPerformance_unknownId(@Min(1) @Max(Long.MAX_VALUE) final long unknownId) {
            // when
            Optional<PerformanceDetailResponse> opPerformanceDetailResponse =
                    performanceRepository.getPerformance(unknownId);

            // then
            assertFalse(opPerformanceDetailResponse.isPresent(), "조회된 PerformanceDetailResponse는 null이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("getPerformances() 테스트")
    class GetPerformancesTests {

        @RepeatedTest(10)
        @DisplayName("PerformanceListResponse 페이징 목록 조회, 검색어 없음")
        void getPerformances() {
            // given
            List<Performance> performances = performanceRepository.saveAll(createPerformances(100));

            String   keyword  = "";
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

            // when
            Page<PerformanceListResponse> performancePage = performanceRepository.getPerformances(keyword, pageable);
            List<PerformanceListResponse> actual          = performancePage.getContent();

            // then
            List<PerformanceListResponse> expected = performances.subList(performances.size() - pageable.getPageSize(),
                                                                          performances.size())
                                                                 .stream()
                                                                 .map(PerformanceListResponse::from)
                                                                 .sorted(Comparator.comparing(
                                                                         PerformanceListResponse::getCreatedAt
                                                                 ).reversed())
                                                                 .toList();

            assertTrue(performancePage.hasContent(), "hasContent는 true이어야 합니다.");
            assertFalse(actual.isEmpty(), "content는 비어있지 않아야 합니다.");
            for (int i = 0; i < expected.size(); i++) {
                PerformanceListResponse actualResponse   = actual.get(i);
                PerformanceListResponse expectedResponse = expected.get(i);

                assertEquals(expectedResponse.getId(), actualResponse.getId(), "id은 같아야 합니다.");
                assertEquals(expectedResponse.getName(), actualResponse.getName(), "name은 같아야 합니다.");
                assertEquals(expectedResponse.getVenue(), actualResponse.getVenue(), "venue은 같아야 합니다.");
                assertTrue(getDiffTime(expectedResponse.getStartTime(),
                                       actualResponse.getStartTime(),
                                       SECONDS) <= 1,
                           "startTime는 1초 이내 차이여야 합니다.");
                assertTrue(getDiffTime(expectedResponse.getEndTime(),
                                       actualResponse.getEndTime(),
                                       SECONDS) <= 1,
                           "endTime는 1초 이내 차이여야 합니다.");
                assertEquals(expectedResponse.getRemainingSeats(), actualResponse.getRemainingSeats(),
                             "remainingSeats은 같아야 합니다.");
                assertEquals(expectedResponse.getTotalSeats(), actualResponse.getTotalSeats(), "totalSeats은 같아야 합니다.");
                assertTrue(getDiffTime(expectedResponse.getCreatedAt(),
                                       actualResponse.getCreatedAt(),
                                       SECONDS) <= 1,
                           "createdAt는 1초 이내 차이여야 합니다.");
                assertTrue(getDiffTime(expectedResponse.getUpdatedAt(),
                                       actualResponse.getUpdatedAt(),
                                       SECONDS) <= 1,
                           "updatedAt는 1초 이내 차이여야 합니다.");
            }
        }

        @RepeatedTest(10)
        @DisplayName("PerformanceListResponse 페이징 목록 조회, 검색어 있음")
        void getPerformancesWithKeyword() {
            // given
            List<Performance> performances = performanceRepository.saveAll(createPerformances(100));

            String   keyword  = FAKER.hobby().activity();
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

            // when
            Page<PerformanceListResponse> performancePage = performanceRepository.getPerformances(keyword, pageable);
            List<PerformanceListResponse> actual          = performancePage.getContent();

            // then
            List<PerformanceListResponse> expected = performances.subList(performances.size() - pageable.getPageSize(),
                                                                          performances.size())
                                                                 .stream()
                                                                 .filter(p -> p.getName().contains(keyword)
                                                                              || p.getInfo().contains(keyword))
                                                                 .map(PerformanceListResponse::from)
                                                                 .sorted(Comparator.comparing(
                                                                         PerformanceListResponse::getCreatedAt
                                                                 ).reversed())
                                                                 .toList();

            for (int i = 0; i < expected.size(); i++) {
                PerformanceListResponse actualResponse   = actual.get(i);
                PerformanceListResponse expectedResponse = expected.get(i);

                assertEquals(expectedResponse.getId(), actualResponse.getId(), "id은 같아야 합니다.");
                assertEquals(expectedResponse.getName(), actualResponse.getName(), "name은 같아야 합니다.");
                assertEquals(expectedResponse.getVenue(), actualResponse.getVenue(), "venue은 같아야 합니다.");
                assertTrue(getDiffTime(expectedResponse.getStartTime(),
                                       actualResponse.getStartTime(),
                                       SECONDS) <= 1,
                           "startTime는 1초 이내 차이여야 합니다.");
                assertTrue(getDiffTime(expectedResponse.getEndTime(),
                                       actualResponse.getEndTime(),
                                       SECONDS) <= 1,
                           "endTime는 1초 이내 차이여야 합니다.");
                assertEquals(expectedResponse.getRemainingSeats(), actualResponse.getRemainingSeats(),
                             "remainingSeats은 같아야 합니다.");
                assertEquals(expectedResponse.getTotalSeats(), actualResponse.getTotalSeats(), "totalSeats은 같아야 합니다.");
                assertTrue(getDiffTime(expectedResponse.getCreatedAt(),
                                       actualResponse.getCreatedAt(),
                                       SECONDS) <= 1,
                           "createdAt는 1초 이내 차이여야 합니다.");
                assertTrue(getDiffTime(expectedResponse.getUpdatedAt(),
                                       actualResponse.getUpdatedAt(),
                                       SECONDS) <= 1,
                           "updatedAt는 1초 이내 차이여야 합니다.");
            }
        }

    }

}
