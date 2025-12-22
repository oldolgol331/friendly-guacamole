package com.example.demo.common.util;

import static com.example.demo.common.response.ErrorCode.INVALID_PAYMENT_APPROVAL_TIME;
import static lombok.AccessLevel.PRIVATE;

import com.example.demo.common.error.BusinessException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PackageName : com.example.demo.common.util
 * FileName    : DateUtils
 * Author      : oldolgol331
 * Date        : 25. 12. 20.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 20.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Slf4j
public abstract class DateUtils {
    /**
     * Unix Timestamp를 LocalDateTime으로 변환합니다.
     *
     * @param unixTimeStr - 타임 스탬프 문자열
     * @return 변환된 LocalDateTime 데이터
     */
    public static LocalDateTime convertUnixToLocalDateTime(final String unixTimeStr) {
        if (unixTimeStr == null || unixTimeStr.isBlank()) return null;

        long unixTime;
        try {
            unixTime = Long.parseLong(unixTimeStr);
        } catch (NumberFormatException e) {
            log.error("Unix 시간 변환 실패 - invalid format: {}", unixTimeStr);
            throw new BusinessException(INVALID_PAYMENT_APPROVAL_TIME);
        }

        Instant instant;
        if (unixTimeStr.length() == 13) instant = Instant.ofEpochMilli(unixTime);
        else instant = Instant.ofEpochSecond(unixTime);

        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}
