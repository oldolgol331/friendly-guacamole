package com.example.demo.domain.performance.dao;

import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceDetailResponse;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceListResponse;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PackageName : com.example.demo.domain.performance.dao
 * FileName    : PerformanceRepositoryCustom
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : Performance 엔티티 커스텀 DAO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
public interface PerformanceRepositoryCustom {

    Optional<PerformanceDetailResponse> getPerformance(Long performanceId);

    Page<PerformanceListResponse> getPerformances(String keyword, Pageable pageable);

}
