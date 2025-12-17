package com.example.demo.domain.reservation.dao;

import com.example.demo.domain.reservation.dto.ReservationResponse.ReservationInfoResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PackageName : com.example.demo.domain.reservation.dao
 * FileName    : ReservationRepositoryCustom
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : Reservation 엔티티 커스텀 DAO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
public interface ReservationRepositoryCustom {

    Page<ReservationInfoResponse> getMyReservations(UUID accountId, Pageable pageable);

}
