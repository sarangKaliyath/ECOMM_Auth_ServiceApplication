package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.dto.*;

public interface IAuthService {

    UserDto signup(SignupRequestDto signupRequestDto);

    UserDto login(LoginRequestDto loginRequestDto);
}
