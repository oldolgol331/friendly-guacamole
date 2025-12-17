package com.example.demo.domain.performance.service;

import static com.example.demo.common.response.ErrorCode.DELETE_NOT_ALLOWED_HAS_RESERVATION;
import static com.example.demo.common.response.ErrorCode.PERFORMANCE_NOT_FOUND;
import static com.example.demo.common.util.TestUtils.createPerformanceCreateRequest;
import static com.example.demo.common.util.TestUtils.createPerformanceDetailResponse;
import static com.example.demo.common.util.TestUtils.createPerformanceListResponses;
import static com.example.demo.common.util.TestUtils.createPerformanceUpdateRequest;
import static com.example.demo.domain.seat.model.SeatStatus.AVAILABLE;
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
import com.example.demo.common.util.TestUtils;
import com.example.demo.domain.performance.dao.PerformanceRepository;
import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceCreateRequest;
import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceUpdateRequest;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceDetailResponse;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceListResponse;
import com.example.demo.domain.performance.model.Performance;
import com.example.demo.domain.seat.dao.SeatRepository;
import java.util.List;
import java.util.Optional;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * PackageName : com.example.demo.domain.performance.service
 * FileName    : PerformanceServiceTest
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : PerformanceService 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @InjectMocks
    PerformanceServiceImpl performanceService;
    @Mock
    PerformanceRepository performanceRepository;
    @Mock
    SeatRepository        seatRepository;

    @Nested
    @DisplayName("createPerformance() 테스트")
    class CreatePerformanceTests {

        @RepeatedTest(10)
        @DisplayName("공연 생성")
        void createPerformance() {
            // given
            PerformanceCreateRequest request = createPerformanceCreateRequest();

            Performance performance = Performance.of(request.getName(),
                                                     request.getVenue(),
                                                     request.getInfo(),
                                                     request.getStartTime(),
                                                     request.getEndTime());
            ReflectionTestUtils.setField(performance, "id", 1L);

            when(performanceRepository.save(any(Performance.class))).thenReturn(performance);

            // when
            performanceService.createPerformance(request);

            // then
            verify(performanceRepository, times(1)).save(any(Performance.class));
            verify(seatRepository, times(1)).saveAll(any(List.class));
        }

    }

    @Nested
    @DisplayName("getAllPerformances() 테스트")
    class GetAllPerformancesTests {

        @RepeatedTest(10)
        @DisplayName("공연 목록 조회")
        void getAllPerformances() {
            // given
            String keyword = "test";
            Pageable pageable = PageRequest.of(0, 10);

            Page<PerformanceListResponse> mockPage = new PageImpl<>(createPerformanceListResponses(5));

            when(performanceRepository.getPerformances(eq(keyword), eq(pageable))).thenReturn(mockPage);

            // when
            Page<PerformanceListResponse> result = performanceService.getAllPerformances(keyword, pageable);

            // then
            assertEquals(mockPage, result);

            verify(performanceRepository, times(1)).getPerformances(eq(keyword), eq(pageable));
        }

        @RepeatedTest(10)
        @DisplayName("공연 목록 조회, 검색어 없음")
        void getAllPerformances_noKeyword() {
            // given
            String keyword = null;
            Pageable pageable = PageRequest.of(0, 10);

            Page<PerformanceListResponse> mockPage = new PageImpl<>(createPerformanceListResponses(5));

            when(performanceRepository.getPerformances(eq(keyword), eq(pageable))).thenReturn(mockPage);

            // when
            Page<PerformanceListResponse> result = performanceService.getAllPerformances(keyword, pageable);

            // then
            assertEquals(mockPage, result);

            verify(performanceRepository, times(1)).getPerformances(eq(keyword), eq(pageable));
        }

    }

    @Nested
    @DisplayName("getPerformance() 테스트")
    class GetPerformanceTests {

        @RepeatedTest(10)
        @DisplayName("공연 상세 조회")
        void getPerformance() {
            // given
            Long performanceId = 1L;
            PerformanceDetailResponse mockResponse = createPerformanceDetailResponse();

            when(performanceRepository.getPerformance(eq(performanceId))).thenReturn(Optional.of(mockResponse));

            // when
            PerformanceDetailResponse result = performanceService.getPerformance(performanceId);

            // then
            assertEquals(mockResponse, result);

            verify(performanceRepository, times(1)).getPerformance(eq(performanceId));
        }

        @RepeatedTest(10)
        @DisplayName("공연 상세 조회 시도, 해당 공연이 없음")
        void getPerformance_notFound() {
            // given
            Long performanceId = 1L;

            when(performanceRepository.getPerformance(eq(performanceId))).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> performanceService.getPerformance(performanceId),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PERFORMANCE_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 PERFORMANCE_NOT_FOUND여야 합니다."));

            verify(performanceRepository, times(1)).getPerformance(eq(performanceId));
        }

    }

    @Nested
    @DisplayName("updatePerformance() 테스트")
    class UpdatePerformanceTests {

        @RepeatedTest(10)
        @DisplayName("공연 정보 수정")
        void updatePerformance() {
            // given
            Long performanceId = 1L;
            PerformanceUpdateRequest request = createPerformanceUpdateRequest();

            Performance performance = TestUtils.createPerformance();
            ReflectionTestUtils.setField(performance, "id", performanceId);

            when(performanceRepository.findById(eq(performanceId))).thenReturn(Optional.of(performance));

            // when
            performanceService.updatePerformance(performanceId, request);

            // then
            assertEquals(request.getName(), performance.getName());
            assertEquals(request.getVenue(), performance.getVenue());
            assertEquals(request.getInfo(), performance.getInfo());
            assertEquals(request.getStartTime(), performance.getStartTime());
            assertEquals(request.getEndTime(), performance.getEndTime());

            verify(performanceRepository, times(1)).findById(eq(performanceId));
        }

        @RepeatedTest(10)
        @DisplayName("공연 정보 수정 시도, 해당 공연이 없음")
        void updatePerformance_notFound() {
            // given
            Long performanceId = 1L;
            PerformanceUpdateRequest request = createPerformanceUpdateRequest();

            when(performanceRepository.findById(eq(performanceId))).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> performanceService.updatePerformance(performanceId, request),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(PERFORMANCE_NOT_FOUND, exception.getErrorCode(),
                                         "errorCode는 PERFORMANCE_NOT_FOUND여야 합니다."));

            verify(performanceRepository, times(1)).findById(eq(performanceId));
        }

    }

    @Nested
    @DisplayName("deletePerformance() 테스트")
    class DeletePerformanceTests {

        @RepeatedTest(10)
        @DisplayName("공연 삭제")
        void deletePerformance() {
            // given
            Long performanceId = 1L;

            when(seatRepository.existsByPerformanceIdAndStatusNot(eq(performanceId), eq(AVAILABLE))).thenReturn(false);

            // when
            performanceService.deletePerformance(performanceId);

            // then
            verify(seatRepository, times(1)).existsByPerformanceIdAndStatusNot(eq(performanceId), eq(AVAILABLE));
            verify(performanceRepository, times(1)).deleteById(eq(performanceId));
        }

        @RepeatedTest(10)
        @DisplayName("공연 삭제 시도, 예약된 좌석이 존재함")
        void deletePerformance_hasReservation() {
            // given
            Long performanceId = 1L;

            when(seatRepository.existsByPerformanceIdAndStatusNot(eq(performanceId), eq(AVAILABLE))).thenReturn(true);

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> performanceService.deletePerformance(performanceId),
                                                       "BusinessException이 발생해야 합니다.");

            // then
            assertAll(() -> assertNotNull(exception, "exception은 null이 아니어야 합니다."),
                      () -> assertEquals(DELETE_NOT_ALLOWED_HAS_RESERVATION, exception.getErrorCode(),
                                         "errorCode는 DELETE_NOT_ALLOWED_HAS_RESERVATION이어야 합니다."));

            verify(seatRepository, times(1)).existsByPerformanceIdAndStatusNot(eq(performanceId), eq(AVAILABLE));
            verify(performanceRepository, never()).deleteById(eq(performanceId));
        }

    }

}
