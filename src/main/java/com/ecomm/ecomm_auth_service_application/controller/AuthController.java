package com.ecomm.ecomm_auth_service_application.controller;

import com.ecomm.ecomm_auth_service_application.dto.*;
import com.ecomm.ecomm_auth_service_application.exception.InvalidTokenException;
import com.ecomm.ecomm_auth_service_application.security.CookieUtils;
import com.ecomm.ecomm_auth_service_application.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupRequestDto signupRequestDto) {
        return new ResponseEntity<>(authService.signup(signupRequestDto), HttpStatus.CREATED);
    }

    // Validates credentials, creates a refresh session, and sets the HttpOnly cookie.
    // No access token is returned here — the frontend must call POST /auth/refresh next.
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequestDto loginRequestDto,
                                      HttpServletResponse response) {
        String refreshToken = authService.login(loginRequestDto);
        response.addHeader(HttpHeaders.SET_COOKIE,
                CookieUtils.refreshTokenCookie(refreshToken).toString());
        return ResponseEntity.noContent().build();
    }

    // Validates the refresh session, rotates the refresh token (old session invalidated,
    // new session created), and returns a short-lived access token.
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDto> refresh(
            @CookieValue(name = "refreshToken", required = false) String rawToken,
            HttpServletResponse response) {

        if (rawToken == null) {
            throw new InvalidTokenException("No refresh token cookie present");
        }

        RefreshResponseDto result = authService.refresh(rawToken);
        response.addHeader(HttpHeaders.SET_COOKIE,
                CookieUtils.refreshTokenCookie(result.getNewRefreshToken()).toString());
        return ResponseEntity.ok(result);
    }

    // Invalidates the refresh session and clears the cookie.
    // Safe to call even when no cookie is present (idempotent).
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String rawToken,
            HttpServletResponse response) {

        if (rawToken != null) {
            authService.logout(rawToken);
        }
        response.addHeader(HttpHeaders.SET_COOKIE,
                CookieUtils.clearRefreshTokenCookie().toString());
        return ResponseEntity.noContent().build();
    }

    // Revokes every active session for the user across all devices/families,
    // not just the one tied to the presented cookie.
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            @CookieValue(name = "refreshToken", required = false) String rawToken,
            HttpServletResponse response) {

        if (rawToken == null) {
            throw new InvalidTokenException("No refresh token cookie present");
        }

        authService.logoutAll(rawToken);
        response.addHeader(HttpHeaders.SET_COOKIE,
                CookieUtils.clearRefreshTokenCookie().toString());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    public void validateToken(@RequestBody TokenValidationRequest req) {
        authService.validateAccessToken(req.getToken());
    }

    // Consumes the one-time resetToken returned by POST /verify/confirm
    // (verificationType=PASSWORD_RESET) and sets the new password.
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request.getResetToken(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
