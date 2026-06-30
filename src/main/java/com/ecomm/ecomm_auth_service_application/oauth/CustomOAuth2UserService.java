package com.ecomm.ecomm_auth_service_application.oauth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        OAuth2User oauthUser = super.loadUser(userRequest);

        System.out.println("Google User Attributes:");
        oauthUser.getAttributes()
                .forEach((key, value) ->
                        System.out.println(key + " : " + value));

        return oauthUser;
    }
}