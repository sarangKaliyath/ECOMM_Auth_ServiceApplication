package com.ecomm.ecomm_auth_service_application.controller;


import com.ecomm.ecomm_auth_service_application.dto.LoginRequestDto;
import com.ecomm.ecomm_auth_service_application.dto.LoginResponseDto;
import com.ecomm.ecomm_auth_service_application.dto.SignupRequestDto;
import com.ecomm.ecomm_auth_service_application.dto.SignupResponseDto;
import com.ecomm.ecomm_auth_service_application.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public SignupResponseDto signup(@RequestBody SignupRequestDto signupRequestDto) {
        return new SignupResponseDto();
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto) {
        return new LoginResponseDto();
    }
}
