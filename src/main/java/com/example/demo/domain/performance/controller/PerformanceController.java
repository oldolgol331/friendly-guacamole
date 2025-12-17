package com.example.demo.domain.performance.controller;

import static com.example.demo.common.response.SuccessCode.DELETE_PERFORMANCE_SUCCESS;
import static com.example.demo.common.response.SuccessCode.PERFORMANCE_CREATE_SUCCESS;
import static com.example.demo.common.response.SuccessCode.PERFORMANCE_LIST_SEARCH_SUCCESS;
import static com.example.demo.common.response.SuccessCode.PERFORMANCE_READ_SUCCESS;
import static com.example.demo.common.response.SuccessCode.UPDATE_PERFORMANCE_INFO_SUCCESS;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.SuccessCode;
import com.example.demo.common.response.annotation.CustomPageResponse;
import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceCreateRequest;
import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceUpdateRequest;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceDetailResponse;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceListResponse;
import com.example.demo.domain.performance.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.example.demo.domain.performance.controller
 * FileName    : PerformanceController
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 공연(Performance) 컨트롤러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@RestController
@RequestMapping("/api/v1/performances")
@RequiredArgsConstructor
@Tag(name = "공연 API", description = "공연 생성, 조회/검색, 수정, 삭제 API를 제공합니다.")
public class PerformanceController {

    private final PerformanceService performanceService;

    @PostMapping
    @Operation(summary = "공연 생성", description = "공연 정보를 등록하고 좌석을 생성합니다.")
    public ResponseEntity<ApiResponse<Void>> createPerformance(
            @Valid @RequestBody final PerformanceCreateRequest request
    ) {
        performanceService.createPerformance(request);
        final SuccessCode successCode = PERFORMANCE_CREATE_SUCCESS;
        return ResponseEntity.status(successCode.getStatus()).body(ApiResponse.success(successCode));
    }

    @GetMapping
    @CustomPageResponse(numberOfElements = false, empty = false, hasContent = false)
    @Operation(summary = "공연 목록 조회", description = "검색어와 페이징을 지원하는 공연 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<PerformanceListResponse>>> getPerformances(
            @RequestParam(required = false) final String keyword,
            @PageableDefault(page = 1,
                             size = 10,
                             sort = "createdAt",
                             direction = DESC) final Pageable pageable
    ) {
        Page<PerformanceListResponse> responseData = performanceService.getAllPerformances(
                keyword,
                PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort())
        );
        return ResponseEntity.ok(ApiResponse.success(PERFORMANCE_LIST_SEARCH_SUCCESS, responseData));
    }

    @GetMapping("/{performanceId}")
    @Operation(summary = "공연 상세 조회", description = "특정 공연의 상세 정보와 좌석 현황을 조회합니다.")
    public ResponseEntity<ApiResponse<PerformanceDetailResponse>> getPerformance(
            @PathVariable("performanceId") @Min(1) final Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(PERFORMANCE_READ_SUCCESS, performanceService.getPerformance(id)));
    }

    @PutMapping("/{performanceId}")
    @Operation(summary = "공연 정보 수정", description = "공연의 메타데이터(이름, 장소, 시간 등)를 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> updatePerformance(@PathVariable("performanceId") @Min(1) final Long id,
                                                               @Valid @RequestBody final PerformanceUpdateRequest request) {
        performanceService.updatePerformance(id, request);
        return ResponseEntity.ok(ApiResponse.success(UPDATE_PERFORMANCE_INFO_SUCCESS));
    }

    @DeleteMapping("/{performanceId}")
    @Operation(summary = "공연 삭제", description = "공연 정보와 좌석을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deletePerformance(@PathVariable("performanceId") @Min(1) final Long id) {
        performanceService.deletePerformance(id);
        return ResponseEntity.ok(ApiResponse.success(DELETE_PERFORMANCE_SUCCESS));
    }

}
