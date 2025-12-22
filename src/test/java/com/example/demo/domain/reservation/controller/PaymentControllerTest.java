package com.example.demo.domain.reservation.controller;

import static com.example.demo.common.response.SuccessCode.PAYMENT_REFUND_SUCCESS;
import static com.example.demo.common.response.SuccessCode.PAYMENT_SUCCESS;
import static com.example.demo.common.response.SuccessCode.PRE_PAYMENT_SAVE_SUCCESS;
import static com.example.demo.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_PREFIX;
import static com.example.demo.common.util.TestUtils.FAKER;
import static com.example.demo.common.util.TestUtils.generatePaymentKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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
import com.example.demo.domain.reservation.dto.PaymentRequest.PaymentCancelRequest;
import com.example.demo.domain.reservation.dto.PaymentRequest.PaymentCompleteRequest;
import com.example.demo.domain.reservation.dto.PaymentRequest.PrePaymentRequest;
import com.example.demo.domain.reservation.dto.PaymentResponse.PrePaymentInfoResponse;
import com.example.demo.domain.reservation.facade.ReservationFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
 * FileName    : PaymentControllerTest
 * Author      : oldolgol331
 * Date        : 25. 12. 22.
 * Description : PaymentController 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 22.   oldolgol331          Initial creation
 */
@WebMvcTest(value = PaymentController.class,
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE,
                                     classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@AutoConfigureWebMvc
@Import({AppConfig.class, PageResponseAdvice.class, TestSecurityConfig.class})
class PaymentControllerTest {

    @Autowired
    MockMvc           mockMvc;
    @Autowired
    ObjectMapper      objectMapper;
    @MockitoBean
    ReservationFacade reservationFacade;

    @RepeatedTest(10)
    @CustomWithMockUser
    @DisplayName("검증용 결제 정보 사전 저장")
    void savePrePayment() throws Exception {
        // given
        PrePaymentRequest request     = new PrePaymentRequest(1L, 1L, "CARD", 10000);
        String            requestBody = objectMapper.writeValueAsString(request);

        String rawAccessToken = UUID.randomUUID().toString().replace("-", "");
        String bearerAccessToken = JWT_ACCESS_TOKEN_PREFIX + rawAccessToken;

        PrePaymentInfoResponse response = new PrePaymentInfoResponse(
                generatePaymentKey(),
                1L,
                1L,
                FAKER.commerce().productName(),
                10000,
                LocalDateTime.now().plusMinutes(30)
        );

        when(reservationFacade.savePrePayment(any(UUID.class), eq(request), anyString())).thenReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/payments/prepare")
                                                             .header(AUTHORIZATION, bearerAccessToken)
                                                             .contentType(APPLICATION_JSON_VALUE)
                                                             .content(requestBody));

        // then
        ApiResponse<PrePaymentInfoResponse> apiResponse = ApiResponse.success(PRE_PAYMENT_SAVE_SUCCESS, response);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(PaymentController.class))
                     .andExpect(handler().methodName("savePrePayment"))
                     .andExpect(status().isCreated())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(reservationFacade, times(1)).savePrePayment(any(UUID.class), eq(request), anyString());
    }

    @RepeatedTest(10)
    @CustomWithMockUser
    @DisplayName("결제 검증 및 완료")
    void completePayment() throws Exception {
        // given
        PaymentCompleteRequest request     = new PaymentCompleteRequest("payment123");
        String                 requestBody = objectMapper.writeValueAsString(request);

        String rawAccessToken = UUID.randomUUID().toString().replace("-", "");
        String bearerAccessToken = JWT_ACCESS_TOKEN_PREFIX + rawAccessToken;

        doNothing().when(reservationFacade).verifyPayment(any(UUID.class), eq(request.getPaymentId()), anyString());

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/payments/complete")
                                                             .header(AUTHORIZATION, bearerAccessToken)
                                                             .contentType(APPLICATION_JSON_VALUE)
                                                             .content(requestBody));

        // then
        ApiResponse<Void> apiResponse = ApiResponse.success(PAYMENT_SUCCESS);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(PaymentController.class))
                     .andExpect(handler().methodName("completePayment"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(reservationFacade, times(1)).verifyPayment(any(UUID.class), eq(request.getPaymentId()), anyString());
    }

    @RepeatedTest(10)
    @CustomWithMockUser
    @DisplayName("결제 취소")
    void refundPayment() throws Exception {
        // given
        PaymentCancelRequest request     = new PaymentCancelRequest("payment123", "고객 요청");
        String               requestBody = objectMapper.writeValueAsString(request);

        String rawAccessToken = UUID.randomUUID().toString().replace("-", "");
        String bearerAccessToken = JWT_ACCESS_TOKEN_PREFIX + rawAccessToken;

        doNothing().when(reservationFacade).refundPayment(any(UUID.class), eq(request));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/payments/refund")
                                                             .header(AUTHORIZATION, bearerAccessToken)
                                                             .contentType(APPLICATION_JSON_VALUE)
                                                             .content(requestBody));

        // then
        ApiResponse<Void> apiResponse = ApiResponse.success(PAYMENT_REFUND_SUCCESS);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(PaymentController.class))
                     .andExpect(handler().methodName("refundPayment"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());

        verify(reservationFacade, times(1)).refundPayment(any(UUID.class), eq(request));
    }

}
