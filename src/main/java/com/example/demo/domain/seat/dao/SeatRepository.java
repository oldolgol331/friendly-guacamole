package com.example.demo.domain.seat.dao;

import com.example.demo.domain.seat.model.Seat;
import com.example.demo.domain.seat.model.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.example.demo.domain.seat.dao
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

    boolean existsByPerformanceIdAndStatusNot(Long performanceId, SeatStatus status);

}
