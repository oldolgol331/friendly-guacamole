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

}
