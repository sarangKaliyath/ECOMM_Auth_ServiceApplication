package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.dto.*;

public interface IAuthService {

    UserDto signup(SignupRequestDto signupRequestDto);

    String login(LoginRequestDto loginRequestDto);

    void validateAccessToken(String token);

    RefreshResponseDto refresh(String rawRefreshToken);

    void logout(String rawRefreshToken);
}
