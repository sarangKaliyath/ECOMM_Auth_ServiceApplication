package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.dto.LoginRequestDto;
import com.ecomm.ecomm_auth_service_application.dto.LoginResponseDto;
import com.ecomm.ecomm_auth_service_application.dto.SignupRequestDto;
import com.ecomm.ecomm_auth_service_application.dto.SignupResponseDto;

public interface AuthService {

    SignupResponseDto signup(SignupRequestDto signupRequestDto);

    LoginResponseDto login(LoginRequestDto loginRequestDto);
}
