package com.example.demo.domain.performance.service;

import com.example.demo.domain.performance.dto.SeatResponse.SeatListResponse;
import java.util.List;

/**
 * PackageName : com.example.demo.domain.performance.service
 * FileName    : SeatService
 * Author      : oldolgol331
 * Date        : 26. 1. 8.
 * Description : 좌석(Seat) 서비스 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 1. 8.     oldolgol331          Initial creation
 */
public interface SeatService {

    List<SeatListResponse> getAllSeatsByPerformance(Long performanceId);

}
