package com.example.demo.domain.reservation.dao;

import com.example.demo.domain.reservation.model.Payment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * PackageName : com.example.demo.domain.reservation.dao
 * FileName    : PaymentRepository
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description : Payment 엔티티 DAO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentKey(String paymentKey);

    @Query("SELECT p FROM Payment p WHERE p.reservation.accountId = :accountId AND p.reservation.seatId = :seatId")
    Optional<Payment> findByReservationId(@Param("accountId") UUID accountId, @Param("seatId") Long seatId);

    List<Payment> findByAccount_Id(UUID accountId);

}
