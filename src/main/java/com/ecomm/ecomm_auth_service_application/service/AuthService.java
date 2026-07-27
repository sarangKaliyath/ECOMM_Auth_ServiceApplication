package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.dto.*;

public interface AuthService {

    UserDto signup(SignupRequestDto signupRequestDto);

    String login(LoginRequestDto loginRequestDto);

    void validateAccessToken(String token);

    RefreshResponseDto refresh(String rawRefreshToken);

    void logout(String rawRefreshToken);

    void logoutAll(String rawRefreshToken);

    // Consumes a one-time reset token issued by VerificationService.verifyCode
    // (PASSWORD_RESET type) and sets the user's new password.
    void resetPassword(String rawResetToken, String newPassword);
}
