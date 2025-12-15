package com.example.demo.infra.security.oauth.model;

import java.util.Map;

/**
 * PackageName : com.example.demo.infra.security.oauth.model
 * FileName    : GoogleProfile
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : Google OAuth 프로필
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
public class GoogleProfile extends OAuthProfile {

    protected GoogleProfile(final Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProviderUserId() {
        return (String) getAttributes().get("sub");
    }

    @Override
    public String getEmail() {
        return (String) getAttributes().get("email");
    }

    @Override
    public String getNickname() {
        return (String) getAttributes().get("name");
    }

    @Override
    public String getProvider() {
        return "GOOGLE";
    }

}
