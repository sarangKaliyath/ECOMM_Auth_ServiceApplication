package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.dto.*;
import com.ecomm.ecomm_auth_service_application.model.User;
import org.antlr.v4.runtime.misc.Pair;

public interface IAuthService {

    UserDto signup(SignupRequestDto signupRequestDto);

    Pair<LoginResponseDto, String> login(LoginRequestDto loginRequestDto);

    void validateAccessToken(String token);
}
