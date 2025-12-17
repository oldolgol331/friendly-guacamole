package com.example.demo.infra.security.oauth.model;

import static com.example.demo.common.response.ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED;
import static lombok.AccessLevel.PRIVATE;

import com.example.demo.common.error.BusinessException;
import java.util.Map;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.infra.security.oauth.model
 * FileName    : OAuthProfileFactory
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : OAuth 프로필 팩토리
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class OAuthProfileFactory {

    public static OAuthProfile getOAuthProfile(final String registrationId, final Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(registrationId)) return new GoogleProfile(attributes);
        if ("naver".equalsIgnoreCase(registrationId)) return new NaverProfile(attributes);
        if ("kakao".equalsIgnoreCase(registrationId)) return new KakaoProfile(attributes);
        throw new BusinessException(OAUTH_PROVIDER_NOT_SUPPORTED);
    }

}
