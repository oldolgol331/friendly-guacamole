package com.example.demo.domain.performance.model;

import static com.example.demo.common.response.ErrorCode.INVALID_PERFORMANCE_DATE;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import com.example.demo.common.error.BusinessException;
import com.example.demo.common.model.BaseAuditingEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PackageName : com.example.demo.domain.performance.model
 * FileName    : Performance
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 공연 정보 엔티티
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Entity
@Table(name = "performances")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Performance extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "performance_id", nullable = false, updatable = false)
    private Long id;                                // ID

    @Column(nullable = false)
    @Setter
    @NotBlank
    private String name;                            // 공연 이름

    @Column(nullable = false)
    @Setter
    @NotBlank
    private String venue;                           // 공연 장소

    @Column(columnDefinition = "TEXT")
    @Setter
    private String info;                            // 공연 정보

    @Column(nullable = false)
    @NotNull
    private LocalDateTime startTime;                // 공연 시작 시간

    @Column(nullable = false)
    @NotNull
    private LocalDateTime endTime;                  // 공연 종료 시간

    @OneToMany(mappedBy = "performance", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();   // 공연 좌석 정보 목록

    private Performance(final String name,
                        final String venue,
                        final String info,
                        final LocalDateTime startTime,
                        final LocalDateTime endTime) {
        this.name = name;
        this.venue = venue;
        this.info = info;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // ========================= 생성자 메서드 =========================

    /**
     * Performance 객체 생성
     *
     * @param name      - 공연명
     * @param venue     - 장소
     * @param info      - 공연 정보
     * @param startTime - 공연 시작 시간
     * @param endTime   - 공연 종료 시간
     * @return Performance 객체
     */
    public static Performance of(final String name,
                                 final String venue,
                                 final String info,
                                 final LocalDateTime startTime,
                                 final LocalDateTime endTime) {
        validatePerformanceTime(startTime, endTime);
        return new Performance(name, venue, info, startTime, endTime);
    }

    // ========================= 검증 메서드 =========================

    /**
     * 공연 시간을 검증합니다.
     *
     * @param inputStartTime - 공연 시작 시간
     * @param inputEndTime   - 공연 종료 시간
     */
    private static void validatePerformanceTime(final LocalDateTime inputStartTime, final LocalDateTime inputEndTime) {
        if (inputStartTime.isAfter(inputEndTime) || inputStartTime.equals(inputEndTime))
            throw new BusinessException(INVALID_PERFORMANCE_DATE);
    }

    // ========================= 비즈니스 메서드 =========================

    /**
     * 공연 시간을 변경합니다.
     *
     * @param newStartTime - 변경할 공연 시작 시간
     * @param newEndTime   - 변경할 공연 종료 시간
     */
    public void setPerformanceTime(final LocalDateTime newStartTime, final LocalDateTime newEndTime) {
        validatePerformanceTime(newStartTime, newEndTime);
        startTime = newStartTime;
        endTime = newEndTime;
    }

}
