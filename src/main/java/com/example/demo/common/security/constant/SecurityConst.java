package com.example.demo.common.security.constant;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.common.security.constant
 * FileName    : SecurityConst
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 시큐리티 관련 상수
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class SecurityConst {

    public static final String JWT_ACCESS_TOKEN_PREFIX       = "Bearer ";
    public static final String JWT_ACCESS_TOKEN_HEADER_NAME  = "Authorization";
    public static final String JWT_REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public static final String JWT_USERNAME_KEY    = "username";
    public static final String JWT_AUTHORITIES_KEY = "authorities";

}
