package com.ecomm.ecomm_auth_service_application.security;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtils {

    public static ResponseCookie refreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    // Used by POST /auth/logout to instruct the browser to delete the cookie.
    public static ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(0)
                .build();
    }
}
