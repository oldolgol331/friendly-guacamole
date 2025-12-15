package com.example.demo.infra.security.oauth.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.example.demo.infra.security.oauth.model
 * FileName    : OAuthProfile
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 공용 OAuth 프로필
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public abstract class OAuthProfile {

    private final Map<String, Object> attributes;

    public abstract String getProviderUserId();

    public abstract String getEmail();

    public abstract String getNickname();

    public abstract String getProvider();

}
