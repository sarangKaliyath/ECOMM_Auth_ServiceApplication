package com.ecomm.ecomm_auth_service_application.config;

import com.ecomm.ecomm_auth_service_application.oauth.CustomOAuth2UserService;
import com.ecomm.ecomm_auth_service_application.oauth.CustomOidcUserService;
import com.ecomm.ecomm_auth_service_application.oauth.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SpringSecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private CustomOidcUserService customOidcUserService;

    @Autowired
    private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
//                .oauth2Login(oauth -> oauth
//                        .userInfoEndpoint(userInfo ->
//                               userInfo.userService(customOAuth2UserService)
//                        )
//                          .userInfoEndpoint(userInfo ->
//                            userInfo.oidcUserService(customOidcUserService)
//                          )
//                );
                .oauth2Login(oauth -> oauth
                        .successHandler(oauth2LoginSuccessHandler)
                        .userInfoEndpoint(userInfo ->
                                userInfo.oidcUserService(customOidcUserService)
                        )
                );

        return httpSecurity.build();
    }
}
