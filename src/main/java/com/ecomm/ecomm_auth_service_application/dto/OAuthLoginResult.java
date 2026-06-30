package com.ecomm.ecomm_auth_service_application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthLoginResult {
    private String refreshToken;
    private boolean newUser;
}
