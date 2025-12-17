package com.example.demo.domain.reservation.controller;

import static com.example.demo.common.response.SuccessCode.RESERVATION_CANCEL_SUCCESS;
import static com.example.demo.common.response.SuccessCode.RESERVATION_CREATE_SUCCESS;
import static com.example.demo.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_PREFIX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.common.config.AppConfig;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.advice.PageResponseAdvice;
import com.example.demo.common.security.annotation.CustomWithMockUser;
import com.example.demo.common.security.config.SecurityConfig;
import com.example.demo.common.security.config.TestSecurityConfig;
import com.example.demo.common.security.jwt.filter.JwtAuthenticationFilter;
import com.example.demo.domain.reservation.dto.ReservationRequest.ReservationCreateRequest;
import com.example.demo.domain.reservation.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * PackageName : com.example.demo.domain.reservation.controller
 * FileName    : ReservationControllerTest
 * Author      : oldolgol331
 * Date        : 25. 12. 17.
 * Description : ReservationController 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 17.   oldolgol331          Initial creation
 */
@WebMvcTest(value = ReservationController.class,
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE,
                                     classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@AutoConfigureWebMvc
@Import({AppConfig.class, PageResponseAdvice.class, TestSecurityConfig.class})
class ReservationControllerTest {

    @Autowired
    MockMvc            mockMvc;
    @Autowired
    ObjectMapper       objectMapper;
    @MockitoBean
    ReservationService reservationService;

    @RepeatedTest(10)
    @CustomWithMockUser
    @DisplayName("좌석 예매")
    void reserveSeat() throws Exception {
        // given
        ReservationCreateRequest request     = new ReservationCreateRequest(1L);
        String                   requestBody = objectMapper.writeValueAsString(request);

        String rawAccessToken    = UUID.randomUUID().toString().replace("-", "");
        String bearerAccessToken = JWT_ACCESS_TOKEN_PREFIX + rawAccessToken;

        doNothing().when(reservationService).reserveSeat(any(UUID.class), eq(request));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/reservations")
                                                              .header(AUTHORIZATION, bearerAccessToken)
                                                              .contentType(APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        // then
        ApiResponse<Void> apiResponse  = ApiResponse.success(RESERVATION_CREATE_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(ReservationController.class))
                     .andExpect(handler().methodName("reserveSeat"))
                     .andExpect(status().isCreated())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(reservationService, times(1)).reserveSeat(any(UUID.class), eq(request));
    }

    @RepeatedTest(10)
    @CustomWithMockUser
    @DisplayName("예매 취소")
    void cancelReservation() throws Exception {
        // given
        Long seatId = 1L;

        String rawAccessToken    = UUID.randomUUID().toString().replace("-", "");
        String bearerAccessToken = JWT_ACCESS_TOKEN_PREFIX + rawAccessToken;

        doNothing().when(reservationService).cancelReservation(any(UUID.class), eq(seatId));

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/reservations/{seatId}", seatId)
                                                              .header(AUTHORIZATION, bearerAccessToken));

        // then
        ApiResponse<Void> apiResponse  = ApiResponse.success(RESERVATION_CANCEL_SUCCESS);
        String            responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(ReservationController.class))
                     .andExpect(handler().methodName("cancelReservation"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(reservationService, times(1)).cancelReservation(any(UUID.class), eq(seatId));
    }

}
