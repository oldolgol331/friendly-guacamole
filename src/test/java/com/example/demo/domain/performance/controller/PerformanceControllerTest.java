package com.example.demo.domain.performance.controller;

import static com.example.demo.common.response.SuccessCode.DELETE_PERFORMANCE_SUCCESS;
import static com.example.demo.common.response.SuccessCode.PERFORMANCE_CREATE_SUCCESS;
import static com.example.demo.common.response.SuccessCode.PERFORMANCE_LIST_SEARCH_SUCCESS;
import static com.example.demo.common.response.SuccessCode.PERFORMANCE_READ_SUCCESS;
import static com.example.demo.common.response.SuccessCode.UPDATE_PERFORMANCE_INFO_SUCCESS;
import static com.example.demo.common.util.TestUtils.FAKER;
import static com.example.demo.common.util.TestUtils.createPerformanceCreateRequest;
import static com.example.demo.common.util.TestUtils.createPerformanceDetailResponse;
import static com.example.demo.common.util.TestUtils.createPerformanceListResponses;
import static com.example.demo.common.util.TestUtils.createPerformanceUpdateRequest;
import static com.example.demo.domain.account.model.AccountRole.ADMIN;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import autoparams.AutoSource;
import autoparams.Repeat;
import com.example.demo.common.config.AppConfig;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.advice.PageResponseAdvice;
import com.example.demo.common.security.annotation.CustomWithMockUser;
import com.example.demo.common.security.config.SecurityConfig;
import com.example.demo.common.security.config.TestSecurityConfig;
import com.example.demo.common.security.jwt.filter.JwtAuthenticationFilter;
import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceCreateRequest;
import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceUpdateRequest;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceDetailResponse;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceListResponse;
import com.example.demo.domain.performance.service.PerformanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * PackageName : com.example.demo.domain.performance.controller
 * FileName    : PerformanceControllerTest
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : PerformanceController 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@WebMvcTest(value = PerformanceController.class,
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE,
                                     classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@AutoConfigureWebMvc
@Import({AppConfig.class, PageResponseAdvice.class, TestSecurityConfig.class})
class PerformanceControllerTest {

    @Autowired
    MockMvc            mockMvc;
    @Autowired
    ObjectMapper       objectMapper;
    @MockitoBean
    PerformanceService performanceService;

    @RepeatedTest(10)
    @CustomWithMockUser(role = ADMIN)
    @DisplayName("공연 생성")
    void createPerformance() throws Exception {
        // given
        PerformanceCreateRequest request     = createPerformanceCreateRequest();
        String                   requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/performances")
                                                              .contentType(APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        // then
        ApiResponse<Void> apiResponse  = ApiResponse.success(PERFORMANCE_CREATE_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(PerformanceController.class))
                     .andExpect(handler().methodName("createPerformance"))
                     .andExpect(status().isCreated())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(performanceService, times(1)).createPerformance(eq(request));
    }

    @RepeatedTest(10)
    @DisplayName("공연 목록 조회")
    void getPerformances() throws Exception {
        // given
        String   keyword  = FAKER.lorem().word();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        Page<PerformanceListResponse> mockPage = new PageImpl<>(createPerformanceListResponses(10), pageable, 10);

        when(performanceService.getAllPerformances(eq(keyword), eq(pageable))).thenReturn(mockPage);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/performances")
                                                              .param("keyword", keyword)
                                                              .param("page", "1")
                                                              .param("size", "10")
                                                              .param("sort", "createdAt,DESC"));

        // then
        resultActions.andExpect(handler().handlerType(PerformanceController.class))
                     .andExpect(handler().methodName("getPerformances"))
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.message").value(PERFORMANCE_LIST_SEARCH_SUCCESS.getMessage()))
                     .andExpect(jsonPath("$.data.content").isArray())
                     .andExpect(jsonPath("$.data.content").isNotEmpty())
                     .andExpect(jsonPath("$.data.total_elements").value(10))
                     .andExpect(jsonPath("$.data.total_pages").exists())
                     .andExpect(jsonPath("$.data.size").exists())
                     .andExpect(jsonPath("$.data.number").exists())
                     .andExpect(jsonPath("$.data.number_of_elements").doesNotExist())
                     .andExpect(jsonPath("$.data.sort").exists())
                     .andExpect(jsonPath("$.data.empty").doesNotExist())
                     .andExpect(jsonPath("$.data.has_content").doesNotExist())
                     .andExpect(jsonPath("$.data.first").exists())
                     .andExpect(jsonPath("$.data.last").exists())
                     .andExpect(jsonPath("$.data.has_previous").exists())
                     .andExpect(jsonPath("$.data.has_next").exists())
                     .andDo(print());

        verify(performanceService, times(1)).getAllPerformances(eq(keyword), eq(pageable));
    }

    @ParameterizedTest
    @Repeat(10)
    @AutoSource
    @DisplayName("공연 상세 조회")
    void getPerformance(@Min(1) @Max(Long.MAX_VALUE) final long performanceId) throws Exception {
        // given
        PerformanceDetailResponse mockResponse = createPerformanceDetailResponse();

        when(performanceService.getPerformance(eq(performanceId))).thenReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/performances/{performanceId}", performanceId));

        // then
        ApiResponse<PerformanceDetailResponse> apiResponse =
                ApiResponse.success(PERFORMANCE_READ_SUCCESS, mockResponse);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(PerformanceController.class))
                     .andExpect(handler().methodName("getPerformance"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(performanceService, times(1)).getPerformance(eq(performanceId));
    }

    @ParameterizedTest
    @Repeat(10)
    @AutoSource
    @CustomWithMockUser(role = ADMIN)
    @DisplayName("공연 정보 수정")
    void updatePerformance(@Min(1) @Max(Long.MAX_VALUE) final long performanceId) throws Exception {
        // given
        PerformanceUpdateRequest request     = createPerformanceUpdateRequest();
        String                   requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/v1/performances/{performanceId}", performanceId)
                                                              .contentType(APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        // then
        ApiResponse<Void> apiResponse  = ApiResponse.success(UPDATE_PERFORMANCE_INFO_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(PerformanceController.class))
                     .andExpect(handler().methodName("updatePerformance"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(performanceService, times(1)).updatePerformance(eq(performanceId), eq(request));
    }

    @ParameterizedTest
    @Repeat(10)
    @AutoSource
    @CustomWithMockUser(role = ADMIN)
    @DisplayName("공연 삭제")
    void deletePerformance(@Min(1) @Max(Long.MAX_VALUE) final long performanceId) throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/performances/{performanceId}", performanceId));

        // then
        ApiResponse<Void> apiResponse  = ApiResponse.success(DELETE_PERFORMANCE_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(PerformanceController.class))
                     .andExpect(handler().methodName("deletePerformance"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(performanceService, times(1)).deletePerformance(eq(performanceId));
    }

}
