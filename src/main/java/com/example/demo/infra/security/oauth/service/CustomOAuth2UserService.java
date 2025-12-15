package com.example.demo.infra.security.oauth.service;

import com.example.demo.infra.security.oauth.model.OAuthProfile;
import com.example.demo.infra.security.oauth.model.OAuthProfileFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * PackageName : com.example.demo.infra.security.oauth.service
 * FileName    : CustomOAuth2UserService
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 커스텀 OAuth2User 서비스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

//    private final AccountService accountService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuthProfile oAuthProfile = OAuthProfileFactory.getOAuthProfile(registrationId, oAuth2User.getAttributes());

        // TODO: OAuth2 인증 방식으로 계정(Account) 엔티티 조회 또는 생성
//        Account account = accountService.findOrCreateAccountForOAuth(oAuthProfile.getProvider(),
//                                                                     oAuthProfile.getProviderUserId(),
//                                                                     oAuthProfile.getEmail(),
//                                                                     oAuthProfile.getNickname());
//
//        return CustomUserDetails.of(account.getId(),
//                                    account.getEmail(),
//                                    account.getRole(),
//                                    account.getStatus(),
//                                    oAuth2User.getAttributes());
        return null;
    }

}
