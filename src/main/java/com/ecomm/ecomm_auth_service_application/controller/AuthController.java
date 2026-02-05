package com.ecomm.ecomm_auth_service_application.controller;


import com.ecomm.ecomm_auth_service_application.dto.*;
import com.ecomm.ecomm_auth_service_application.model.User;
import com.ecomm.ecomm_auth_service_application.service.IAuthService;

import static com.ecomm.ecomm_auth_service_application.mapper.UserMapper.toResponse;

import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupRequestDto signupRequestDto) {
        return new ResponseEntity<>(authService.signup(signupRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        Pair<LoginResponseDto, String> response = authService.login(loginRequestDto);

        LoginResponseDto loginResponseDto = response.a;
        String refreshToken = response.b;

        HttpHeaders headers = new HttpHeaders();

        // Refresh token is stored in cookies to prevent XSS attacks
        headers.add(HttpHeaders.SET_COOKIE, "refreshToken=" + refreshToken + "; HttpOnly; Secure; SameSite=Strict; Path=/auth/refresh; Max-Age=604800");

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(loginResponseDto);
    }

    @PostMapping("/validate")
    public void validateToken(@RequestBody TokenValidationRequest req) {
        authService.validateAccessToken(req.getToken());
    }
}

