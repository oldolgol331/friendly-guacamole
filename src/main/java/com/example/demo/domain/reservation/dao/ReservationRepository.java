package com.example.demo.domain.reservation.dao;

import com.example.demo.domain.reservation.model.Reservation;
import com.example.demo.domain.reservation.model.ReservationId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.example.demo.domain.reservation.dao
 * FileName    : ReservationRepository
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : Reservation 엔티티 DAO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
public interface ReservationRepository extends JpaRepository<Reservation, ReservationId>, ReservationRepositoryCustom {
}
