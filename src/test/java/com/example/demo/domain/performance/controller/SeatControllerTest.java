package com.example.demo.domain.performance.controller;

import static com.example.demo.common.response.SuccessCode.SEAT_LIST_READ_SUCCESS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import autoparams.AutoSource;
import autoparams.Repeat;
import com.example.demo.common.config.AppConfig;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.advice.PageResponseAdvice;
import com.example.demo.common.security.config.SecurityConfig;
import com.example.demo.common.security.config.TestSecurityConfig;
import com.example.demo.common.security.jwt.filter.JwtAuthenticationFilter;
import com.example.demo.common.util.TestUtils;
import com.example.demo.domain.performance.dto.SeatResponse.SeatListResponse;
import com.example.demo.domain.performance.service.SeatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * PackageName : com.example.demo.domain.performance.controller
 * FileName    : SeatControllerTest
 * Author      : oldolgol331
 * Date        : 26. 1. 8.
 * Description : SeatController 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 1. 8.     oldolgol331          Initial creation
 */
@WebMvcTest(value = SeatController.class,
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE,
                                     classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@AutoConfigureWebMvc
@Import({AppConfig.class, PageResponseAdvice.class, TestSecurityConfig.class})
class SeatControllerTest {

    @Autowired
    MockMvc      mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    SeatService  seatService;

    @ParameterizedTest
    @Repeat(10)
    @AutoSource
    @DisplayName("좌석 목록 조회")
    void getSeats(@Min(1) @Max(Long.MAX_VALUE) final long performanceId) throws Exception {
        // given
        List<SeatListResponse> response = TestUtils.createSeatListResponses(10);

        when(seatService.getAllSeatsByPerformance(eq(performanceId))).thenReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/performances/{performanceId}/seats", performanceId));

        // then
        ApiResponse<List<SeatListResponse>> apiResponse  = ApiResponse.success(SEAT_LIST_READ_SUCCESS, response);
        String                              responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(SeatController.class))
                     .andExpect(handler().methodName("getSeats"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(seatService, times(1)).getAllSeatsByPerformance(eq(performanceId));
    }

}
