package com.example.demo.domain.performance.dao;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;
import static org.hibernate.jpa.SpecHints.HINT_SPEC_LOCK_TIMEOUT;

import com.example.demo.domain.performance.model.Seat;
import com.example.demo.domain.performance.model.SeatStatus;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

/**
 * PackageName : com.example.demo.domain.performance.dao
 * FileName    : SeatRepository
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : Seat 엔티티 DAO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    @QueryHints({@QueryHint(name = HINT_SPEC_LOCK_TIMEOUT, value = "3000")})
    Optional<Seat> findByIdWithLock(@Param("id") Long id);

    boolean existsByPerformanceIdAndStatusNot(Long performanceId, SeatStatus status);

}
