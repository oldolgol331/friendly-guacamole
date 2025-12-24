package com.example.demo.common.util;

import static com.example.demo.common.util.DateUtils.convertUnixToLocalDateTime;
import static java.time.Instant.ofEpochSecond;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.common.error.BusinessException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * PackageName : com.example.demo.common.util
 * FileName    : DateUtilsTest
 * Author      : oldolgol331
 * Date        : 25. 12. 24.
 * Description : DateUtils 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 24.   oldolgol331          Initial creation
 */
class DateUtilsTest {

    @Nested
    @DisplayName("convertUnixToLocalDateTime() 테스트")
    class ConvertUnixToLocalDateTimeTests {

        @RepeatedTest(10)
        @DisplayName("Unix Timestamp(초)를 LocalDateTime으로 변환")
        void convertUnixToLocalDateTime_Seconds() {
            // given
            String unixTimeStr = "1704067200"; // 2024-01-01 00:00:00 UTC

            // when
            LocalDateTime result = convertUnixToLocalDateTime(unixTimeStr);

            // then
            assertEquals(2024, result.getYear());
            assertEquals(1, result.getMonthValue());
            assertEquals(1, result.getDayOfMonth());
        }

        @RepeatedTest(10)
        @DisplayName("Unix Timestamp(밀리초)를 LocalDateTime으로 변환")
        void convertUnixToLocalDateTime_Milliseconds() {
            // given
            String unixTimeStr = "1704067200000"; // 2024-01-01 00:00:00 UTC

            // when
            LocalDateTime result = convertUnixToLocalDateTime(unixTimeStr);

            // then
            assertEquals(2024, result.getYear());
            assertEquals(1, result.getMonthValue());
            assertEquals(1, result.getDayOfMonth());
        }

        @RepeatedTest(10)
        @DisplayName("null Unix Timestamp 처리")
        void convertUnixToLocalDateTime_Null() {
            // given
            String unixTimeStr = null;

            // when
            LocalDateTime result = convertUnixToLocalDateTime(unixTimeStr);

            // then
            assertNull(result);
        }

        @RepeatedTest(10)
        @DisplayName("blank Unix Timestamp 처리")
        void convertUnixToLocalDateTime_Blank() {
            // given
            String unixTimeStr = "   ";

            // when
            LocalDateTime result = convertUnixToLocalDateTime(unixTimeStr);

            // then
            assertNull(result);
        }

        @RepeatedTest(10)
        @DisplayName("빈 문자열 Unix Timestamp 처리")
        void convertUnixToLocalDateTime_Empty() {
            // given
            String unixTimeStr = "";

            // when
            LocalDateTime result = convertUnixToLocalDateTime(unixTimeStr);

            // then
            assertNull(result);
        }

        @RepeatedTest(10)
        @DisplayName("잘못된 형식의 Unix Timestamp 처리")
        void convertUnixToLocalDateTime_InvalidFormat() {
            // given
            String unixTimeStr = "invalid_timestamp";

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                                                       () -> convertUnixToLocalDateTime(unixTimeStr));

            // Unix 타임스탬프가 유효하지 않음을 확인
        }

        @RepeatedTest(10)
        @DisplayName("음수 Unix Timestamp 처리")
        void convertUnixToLocalDateTime_Negative() {
            // given
            String unixTimeStr = "-1704067200";

            // when
            LocalDateTime result = convertUnixToLocalDateTime(unixTimeStr);

            // then
            assertEquals(1916, result.getYear()); // 1970년 이전의 날짜가 되어야 함
        }

        @RepeatedTest(10)
        @DisplayName("매우 큰 Unix Timestamp 처리")
        void convertUnixToLocalDateTime_VeryLarge() {
            // given
            String unixTimeStr = "2147483647"; // 2038년 문제 관련 값

            // when
            LocalDateTime result = convertUnixToLocalDateTime(unixTimeStr);

            // then
            assertEquals(2038, result.getYear());
        }

        @Test
        @DisplayName("정확한 변환 테스트 - 현재 시간 기반")
        void convertUnixToLocalDateTime_Accurate() {
            // given
            long   currentTimeSeconds = System.currentTimeMillis() / 1000;
            String unixTimeStr        = String.valueOf(currentTimeSeconds);

            // when
            LocalDateTime result = convertUnixToLocalDateTime(unixTimeStr);
            LocalDateTime expected = LocalDateTime.ofInstant(ofEpochSecond(currentTimeSeconds), ZoneOffset.UTC)
                                                  .atZone(ZoneId.systemDefault())
                                                  .toLocalDateTime();

            // then
            assertEquals(expected.getYear(), result.getYear());
            assertEquals(expected.getMonth(), result.getMonth());
            assertEquals(expected.getDayOfMonth(), result.getDayOfMonth());
            assertEquals(expected.getHour(), result.getHour());
            assertEquals(expected.getMinute(), result.getMinute());
        }

    }

}