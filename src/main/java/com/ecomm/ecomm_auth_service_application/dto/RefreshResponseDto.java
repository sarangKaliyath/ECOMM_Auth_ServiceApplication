package com.ecomm.ecomm_auth_service_application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshResponseDto {

    // Carried to the controller to set the Set-Cookie header; never serialised to JSON.
    @JsonIgnore
    private String newRefreshToken;

    private String accessToken;
    private long expiresIn;
}
