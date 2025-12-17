package com.example.demo.domain.performance.service;

import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceCreateRequest;
import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceUpdateRequest;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceDetailResponse;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PackageName : com.example.demo.domain.performance.service
 * FileName    : PerformanceService
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 공연(Performance) 서비스 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
public interface PerformanceService {

    void createPerformance(PerformanceCreateRequest request);

    Page<PerformanceListResponse> getAllPerformances(String keyword, Pageable pageable);

    PerformanceDetailResponse getPerformance(Long performanceId);

    void updatePerformance(Long performanceId, PerformanceUpdateRequest request);

    void deletePerformance(Long performanceId);

}
