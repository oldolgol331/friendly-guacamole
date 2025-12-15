package com.example.demo.domain.performance.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import com.example.demo.common.model.BaseAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
    private Long id;

    @Column(nullable = false)
    @Setter
    @NotBlank
    private String name;                // 공연명

    @Column(nullable = false)
    @Setter
    @NotBlank
    private String venue;               // 장소

    @Column(columnDefinition = "TEXT")
    @Setter
    private String info;             // 공연 정보

    @Column(nullable = false)
    @Setter
    @NotNull
    private LocalDateTime startTime;    // 공연 시작 시간

    @Column(nullable = false)
    @Setter
    @NotNull
    private LocalDateTime endTime;      // 공연 종료 시간

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
        return new Performance(name, venue, info, startTime, endTime);
    }

}
