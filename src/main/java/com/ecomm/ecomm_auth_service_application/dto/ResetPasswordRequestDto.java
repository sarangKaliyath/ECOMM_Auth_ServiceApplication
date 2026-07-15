package com.ecomm.ecomm_auth_service_application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDto {
    private String resetToken;
    private String newPassword;
}
