package com.example.demo.domain.performance.dao;

import static com.example.demo.domain.performance.model.SeatStatus.AVAILABLE;

import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceDetailResponse;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceListResponse;
import com.example.demo.domain.performance.dto.QPerformanceResponse_PerformanceDetailResponse;
import com.example.demo.domain.performance.dto.QPerformanceResponse_PerformanceListResponse;
import com.example.demo.domain.performance.model.QPerformance;
import com.example.demo.domain.performance.model.QSeat;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * PackageName : com.example.demo.domain.performance.dao
 * FileName    : PerformanceRepositoryImpl
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : Performance 엔티티 커스텀 DAO 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@Repository
@RequiredArgsConstructor
public class PerformanceRepositoryImpl implements PerformanceRepositoryCustom {

    private static final QPerformance PERFORMANCE = QPerformance.performance;
    private static final QSeat        SEAT        = QSeat.seat;

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 공연 상세 정보를 조회합니다.
     *
     * @param performanceId - 공연 ID
     * @return 공연 상세 정보 응답 DTO
     */
    @Override
    public Optional<PerformanceDetailResponse> getPerformance(final Long performanceId) {
        return Optional.ofNullable(jpaQueryFactory.select(new QPerformanceResponse_PerformanceDetailResponse(
                                                          PERFORMANCE.id,
                                                          PERFORMANCE.name,
                                                          PERFORMANCE.venue,
                                                          PERFORMANCE.info,
                                                          PERFORMANCE.startTime,
                                                          PERFORMANCE.endTime,
                                                          new CaseBuilder().when(SEAT.status.eq(AVAILABLE))
                                                                           .then(1)
                                                                           .otherwise(0)
                                                                           .sum()
                                                                           .intValue(),
                                                          SEAT.id.count().intValue(),
                                                          PERFORMANCE.createdAt,
                                                          PERFORMANCE.updatedAt))
                                                  .from(PERFORMANCE)
                                                  .leftJoin(PERFORMANCE.seats, SEAT)
                                                  .where(PERFORMANCE.id.eq(performanceId))
                                                  .groupBy(PERFORMANCE.id)
                                                  .fetchOne());
    }

    /**
     * 공연 검색 결과를 조회합니다.
     *
     * @param keyword  - 검색어
     * @param pageable - 페이징 객체
     * @return 공연 페이징 목록 응답 DTO
     */
    @Override
    public Page<PerformanceListResponse> getPerformances(final String keyword, final Pageable pageable) {
        List<Long> ids = jpaQueryFactory.select(PERFORMANCE.id)
                                        .from(PERFORMANCE)
                                        .where(matchAgainstKeyword(keyword))
                                        .orderBy(getSortCondition(pageable))
                                        .offset(pageable.getOffset())
                                        .limit(pageable.getPageSize())
                                        .fetch();
        if (ids.isEmpty()) return Page.empty();

        List<PerformanceListResponse> content = jpaQueryFactory.select(
                                                                       new QPerformanceResponse_PerformanceListResponse(PERFORMANCE.id,
                                                                                                                        PERFORMANCE.name,
                                                                                                                        PERFORMANCE.venue,
                                                                                                                        PERFORMANCE.startTime,
                                                                                                                        PERFORMANCE.endTime,
                                                                                                                        new CaseBuilder().when(SEAT.status.eq(AVAILABLE))
                                                                                                                                         .then(1)
                                                                                                                                         .otherwise(0)
                                                                                                                                         .sum()
                                                                                                                                         .intValue(),
                                                                                                                        SEAT.id.count().intValue(),
                                                                                                                        PERFORMANCE.createdAt,
                                                                                                                        PERFORMANCE.updatedAt)
                                                               )
                                                               .from(PERFORMANCE)
                                                               .leftJoin(PERFORMANCE.seats, SEAT)
                                                               .where(PERFORMANCE.id.in(ids))
                                                               .groupBy(PERFORMANCE.id)
                                                               .orderBy(getSortCondition(pageable))
                                                               .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(PERFORMANCE.id.count())
                                                   .from(PERFORMANCE)
                                                   .where(matchAgainstKeyword(keyword));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // ========================= 내부 메서드 =========================

    /**
     * 검색어와 일치하는 게시글을 찾는데 사용되는 조건식(FULLTEXT INDEX MATCH() AGAINST() BOOLEAN MODE)입니다.
     *
     * @param keyword - 검색어
     * @return 조건식
     */
    private BooleanExpression matchAgainstKeyword(final String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        return Expressions.numberTemplate(Double.class,
                                          "function('fulltext_boolean_search_param_2', {0}, {1}, {2})",
                                          PERFORMANCE.name,
                                          PERFORMANCE.info,
                                          "+" + keyword.replaceAll("\\s+", " +"))
                          .gt(0);
    }

    /**
     * 정렬 조건을 생성합니다. 기본적으로 최신 공연부터 조회됩니다.
     *
     * @param pageable - 페이징 정보
     * @return 정렬 조건 배열
     */
    private OrderSpecifier<?>[] getSortCondition(final Pageable pageable) {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();

        boolean hasCreatedAt = pageable.getSort().stream().anyMatch(order -> "createdAt".equals(order.getProperty()));

        if (!pageable.getSort().isEmpty())
            pageable.getSort().forEach(order -> {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
                switch (order.getProperty()) {
                    case "startTime":
                        orderSpecifiers.add(new OrderSpecifier(direction, PERFORMANCE.startTime));
                        break;
                    case "endTime":
                        orderSpecifiers.add(new OrderSpecifier(direction, PERFORMANCE.endTime));
                        break;
                    case "createdAt":
                        orderSpecifiers.add(new OrderSpecifier(direction, PERFORMANCE.createdAt));
                        break;
                    default:
                        break;
                }
            });

        if (!hasCreatedAt) orderSpecifiers.add(new OrderSpecifier(Order.DESC, PERFORMANCE.createdAt));

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

}
