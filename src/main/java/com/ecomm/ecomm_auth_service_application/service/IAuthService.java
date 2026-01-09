package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.dto.LoginRequestDto;
import com.ecomm.ecomm_auth_service_application.dto.LoginResponseDto;
import com.ecomm.ecomm_auth_service_application.dto.SignupRequestDto;
import com.ecomm.ecomm_auth_service_application.dto.SignupResponseDto;
import org.springframework.stereotype.Service;

@Service
public class IAuthService implements AuthService {

    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        return null;
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        return null;
    }
}
