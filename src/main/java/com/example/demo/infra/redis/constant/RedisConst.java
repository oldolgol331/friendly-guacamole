package com.example.demo.infra.redis.constant;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.infra.redis.constant
 * FileName    : RedisConst
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : Redis 관련 상수
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class RedisConst {

    // 시큐리티 관련
    public static final String REDIS_REFRESH_TOKEN_PREFIX          = "rt:";
    public static final String REDIS_ACCESS_TOKEN_BLACKLIST_PREFIX = "bt:";

    // 계정 이메일 관련
    public static final String REDIS_VERIFICATION_KEY_PREFIX              = "email-verification:";
    public static final String REDIS_VERIFICATION_RATE_LIMIT_KEY_PREFIX   = "rate-limit:resend-verification-email:";
    public static final String REDIS_PASSWORD_RESET_KEY_PREFIX            = "password-reset:";
    public static final String REDIS_PASSWORD_RESET_RATE_LIMIT_KEY_PREFIX = "rate-limit:password-reset-email:";

    // 결제 관련
    public static final String REDIS_PRE_PAYMENT_KEY_PREFIX     = "pre-payment:%s";
    public static final int    REDIS_PRE_PAYMENT_EXPIRE_MINUTES = 10;

    // 락 관련
    public static final int  REDISSON_MAX_UNLOCK_RETRY_COUNT = 3;
    public static final long REDISSON_RETRY_DELAY_MILLIS     = 100L;

}
