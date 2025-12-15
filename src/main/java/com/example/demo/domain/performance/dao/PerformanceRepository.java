package com.example.demo.domain.performance.dao;

import com.example.demo.domain.performance.model.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.example.demo.domain.performance.dao
 * FileName    : PerformanceRepository
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : Performance 엔티티 DAO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
public interface PerformanceRepository extends JpaRepository<Performance, Long> {
}
