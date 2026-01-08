package com.example.demo.domain.performance.service;

import static com.example.demo.common.util.TestUtils.createPerformance;
import static com.example.demo.common.util.TestUtils.createSeats;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import autoparams.AutoSource;
import autoparams.Repeat;
import com.example.demo.domain.performance.dao.SeatRepository;
import com.example.demo.domain.performance.dto.SeatResponse.SeatListResponse;
import com.example.demo.domain.performance.model.Seat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * PackageName : com.example.demo.domain.performance.service
 * FileName    : SeatServiceTest
 * Author      : oldolgol331
 * Date        : 26. 1. 8.
 * Description : SeatService 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 1. 8.     oldolgol331          Initial creation
 */
@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @InjectMocks
    SeatServiceImpl seatService;
    @Mock
    SeatRepository  seatRepository;

    @Nested
    @DisplayName("getAllSeatsByPerformance() 테스트")
    class GetAllSeatsByPerformanceTests {

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("공연 좌석 목록 조회")
        void getAllSeatsByPerformance(@Min(1) @Max(Long.MAX_VALUE) final long performanceId) {
            // given
            int        size  = 10;
            List<Seat> seats = createSeats(createPerformance(), size);

            when(seatRepository.findAllByPerformanceId(eq(performanceId))).thenReturn(seats);

            // when
            List<SeatListResponse> actualResponses = seatService.getAllSeatsByPerformance(performanceId);

            // then
            List<SeatListResponse> expectedResponses = seats.stream().map(SeatListResponse::from).toList();

            IntStream.range(0, size).forEach(i -> {
                SeatListResponse expected = expectedResponses.get(i);
                SeatListResponse actual   = actualResponses.get(i);
                assertEquals(expected.getId(), actual.getId(), "id는 같아야 합니다.");
                assertEquals(expected.getSeatCode(), actual.getSeatCode(), "seatCode는 같아야 합니다.");
                assertEquals(expected.getPrice(), actual.getPrice(), "price는 같아야 합니다");
                assertEquals(expected.getStatus(), actual.getStatus(), "status는 같아야 합니다.");
            });
        }

    }

}
