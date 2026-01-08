package com.example.demo.domain.performance.service;

import com.example.demo.domain.performance.dao.SeatRepository;
import com.example.demo.domain.performance.dto.SeatResponse.SeatListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.example.demo.domain.performance.service
 * FileName    : SeatServiceImpl
 * Author      : oldolgol331
 * Date        : 26. 1. 8.
 * Description : 좌석(Seat) 서비스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 1. 8.     oldolgol331          Initial creation
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    /**
     * 공연 좌석 목록을 조회합니다.
     *
     * @param performanceId - 공연 ID
     * @return 좌석 목록 응답 DTO
     */
    @Override
    public List<SeatListResponse> getAllSeatsByPerformance(final Long performanceId) {
        return seatRepository.findAllByPerformanceId(performanceId).stream().map(SeatListResponse::from).toList();
    }

}
