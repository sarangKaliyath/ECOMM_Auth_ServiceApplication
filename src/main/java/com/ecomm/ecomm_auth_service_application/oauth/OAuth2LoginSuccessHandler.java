package com.ecomm.ecomm_auth_service_application.oauth;

import com.ecomm.ecomm_auth_service_application.dto.OAuthLoginResult;
import com.ecomm.ecomm_auth_service_application.security.CookieUtils;
import com.ecomm.ecomm_auth_service_application.service.impl.GoogleOAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final GoogleOAuthService googleOAuthService;
    private final String frontendRedirectUrl;

    public OAuth2LoginSuccessHandler(
            GoogleOAuthService googleOAuthService,
            @Value("${oauth.redirect.url}") String frontendRedirectUrl) {
        this.googleOAuthService = googleOAuthService;
        this.frontendRedirectUrl = frontendRedirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        log.info("===== OAUTH2 SUCCESS HANDLER CALLED ===== principal type: {}",
                authentication.getPrincipal().getClass().getName());

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        OAuthLoginResult result = googleOAuthService.processLogin(oidcUser);

        log.info("OAuth login complete. email={}, isNewUser={}",
                oidcUser.getEmail(), result.isNewUser());

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                CookieUtils.refreshTokenCookie(result.getRefreshToken()).toString()
        );

        response.sendRedirect(frontendRedirectUrl);
    }
}
