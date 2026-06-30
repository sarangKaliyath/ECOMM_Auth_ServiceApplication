package com.ecomm.ecomm_auth_service_application.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        log.info("===== SUCCESS HANDLER CALLED ===== principal type: {}", authentication.getPrincipal().getClass().getName());

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        log.info("===== GOOGLE LOGIN SUCCESS =====");
        log.info("Email: {}", oidcUser.getEmail());
        log.info("Name: {}", oidcUser.getFullName());

        response.sendRedirect("/");
    }
}