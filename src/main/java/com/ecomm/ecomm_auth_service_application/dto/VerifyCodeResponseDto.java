package com.ecomm.ecomm_auth_service_application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyCodeResponseDto {
    private String resetToken;
}
