package com.example.demo.domain.reservation.dao;

import com.example.demo.domain.account.model.QAccount;
import com.example.demo.domain.performance.model.QPerformance;
import com.example.demo.domain.performance.model.QSeat;
import com.example.demo.domain.reservation.dto.QReservationResponse_ReservationInfoResponse;
import com.example.demo.domain.reservation.dto.ReservationResponse.ReservationInfoResponse;
import com.example.demo.domain.reservation.model.QReservation;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

/**
 * PackageName : com.example.demo.domain.reservation.dao
 * FileName    : ReservationRepositoryImpl
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : Reservation 엔티티 커스텀 DAO 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private static final QReservation RESERVATION = QReservation.reservation;
    private static final QPerformance PERFORMANCE = QPerformance.performance;
    private static final QAccount     ACCOUNT     = QAccount.account;
    private static final QSeat        SEAT        = QSeat.seat;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ReservationInfoResponse> getMyReservations(final UUID accountId, final Pageable pageable) {
        List<Long> seatIds = jpaQueryFactory.select(RESERVATION.seatId)
                                            .from(RESERVATION)
                                            .where(RESERVATION.accountId.eq(accountId))
                                            .orderBy(getSortCondition(pageable))
                                            .offset(pageable.getOffset())
                                            .limit(pageable.getPageSize())
                                            .fetch();
        if (seatIds.isEmpty()) return Page.empty();

        List<ReservationInfoResponse> content = jpaQueryFactory.select(
                                                                       new QReservationResponse_ReservationInfoResponse(PERFORMANCE.id,
                                                                                                                        SEAT.id,
                                                                                                                        ACCOUNT.id,
                                                                                                                        ACCOUNT.nickname,
                                                                                                                        PERFORMANCE.name,
                                                                                                                        PERFORMANCE.startTime,
                                                                                                                        PERFORMANCE.endTime,
                                                                                                                        SEAT.seatCode,
                                                                                                                        SEAT.price,
                                                                                                                        RESERVATION.status,
                                                                                                                        RESERVATION.expiredAt,
                                                                                                                        RESERVATION.confirmedAt)
                                                               )
                                                               .from(RESERVATION)
                                                               .leftJoin(RESERVATION.account, ACCOUNT)
                                                               .leftJoin(RESERVATION.seat, SEAT)
                                                               .leftJoin(SEAT.performance, PERFORMANCE)
                                                               .where(ACCOUNT.id.eq(accountId), SEAT.id.in(seatIds))
                                                               .groupBy(RESERVATION.accountId, RESERVATION.seatId)
                                                               .orderBy(getSortCondition(pageable))
                                                               .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(RESERVATION.count())
                                                   .from(RESERVATION)
                                                   .where(RESERVATION.accountId.eq(accountId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // ========================= 내부 메서드 =========================

    /**
     * 정렬 조건을 생성합니다. 기본적으로 최신 예약부터 조회됩니다.
     *
     * @param pageable - 페이징 정보
     * @return 정렬 조건 배열
     */
    private OrderSpecifier<?>[] getSortCondition(final Pageable pageable) {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();

        if (!pageable.getSort().isEmpty())
            pageable.getSort().forEach(order -> {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
                switch (order.getProperty()) {
                    case "createdAt":
                        orderSpecifiers.add(new OrderSpecifier(direction, RESERVATION.createdAt));
                        break;
                    default:
                        break;
                }
            });

        boolean hasCreatedAt = orderSpecifiers.stream().anyMatch(spec -> spec.getTarget().equals(RESERVATION.createdAt));

        if (!hasCreatedAt) orderSpecifiers.add(new OrderSpecifier(Order.DESC, RESERVATION.createdAt));

        orderSpecifiers.add(RESERVATION.seatId.desc());

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

}
