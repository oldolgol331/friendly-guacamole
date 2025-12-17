package com.example.demo.domain.performance.service;

import static com.example.demo.common.response.ErrorCode.DELETE_NOT_ALLOWED_HAS_RESERVATION;
import static com.example.demo.common.response.ErrorCode.PERFORMANCE_NOT_FOUND;
import static com.example.demo.domain.performance.model.SeatStatus.AVAILABLE;

import com.example.demo.common.error.BusinessException;
import com.example.demo.domain.performance.dao.PerformanceRepository;
import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceCreateRequest;
import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceUpdateRequest;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceDetailResponse;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceListResponse;
import com.example.demo.domain.performance.model.Performance;
import com.example.demo.domain.performance.dao.SeatRepository;
import com.example.demo.domain.performance.model.Seat;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.example.demo.domain.performance.service
 * FileName    : PerformanceServiceImpl
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 공연(Performance) 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final SeatRepository        seatRepository;

    /**
     * 공연과 공연 좌석을 생성합니다.
     *
     * @param request - 공연 생성 요청 DTO
     */
    @Transactional
    @Override
    public void createPerformance(final PerformanceCreateRequest request) {
        Performance performance = Performance.of(request.getName(),
                                                 request.getVenue(),
                                                 request.getInfo(),
                                                 request.getStartTime(),
                                                 request.getEndTime());
        performanceRepository.save(performance);

        List<Seat> seats = IntStream.range(1, request.getTotalSeats() + 1)
                                    .boxed()
                                    .map(i -> Seat.of("A-" + i, request.getPrice(), performance))
                                    .toList();
        seatRepository.saveAll(seats);
    }

    /**
     * 공연 목록을 조회합니다.
     *
     * @param keyword  - 검색어
     * @param pageable - 페이징 객체
     * @return 공연 페이징 목록 응답 DTO
     */
    @Override
    public Page<PerformanceListResponse> getAllPerformances(final String keyword, final Pageable pageable) {
        return performanceRepository.getPerformances(keyword, pageable);
    }

    /**
     * 공연 상세 정보를 조회합니다.
     *
     * @param performanceId - 공연 ID
     * @return 공연 상세 정보 응답 DTO
     */
    @Override
    public PerformanceDetailResponse getPerformance(final Long performanceId) {
        return performanceRepository.getPerformance(performanceId)
                                    .orElseThrow(() -> new BusinessException(PERFORMANCE_NOT_FOUND));
    }

    /**
     * 공연 정보를 수정합니다.
     *
     * @param performanceId - 공연 ID
     * @param request       - 공연 수정 요청 DTO
     */
    @Transactional
    @Override
    public void updatePerformance(final Long performanceId, final PerformanceUpdateRequest request) {
        Performance performance = performanceRepository.findById(performanceId)
                                                       .orElseThrow(() -> new BusinessException(PERFORMANCE_NOT_FOUND));

        performance.setName(request.getName());
        performance.setVenue(request.getVenue());
        performance.setInfo(request.getInfo());
        performance.setPerformanceTime(request.getStartTime(), request.getEndTime());
    }

    /**
     * 공연 정보를 삭제합니다.
     *
     * @param performanceId - 공연 ID
     */
    @Transactional
    @Override
    public void deletePerformance(final Long performanceId) {
        if (seatRepository.existsByPerformanceIdAndStatusNot(performanceId,
                                                             AVAILABLE)) // 이미 예매되거나 판매된 좌석이 하나라도 있다면 삭제 불가능
            throw new BusinessException(DELETE_NOT_ALLOWED_HAS_RESERVATION);

        performanceRepository.deleteById(performanceId);
    }

}
