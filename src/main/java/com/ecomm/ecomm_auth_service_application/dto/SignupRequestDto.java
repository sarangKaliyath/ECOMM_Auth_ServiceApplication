package com.ecomm.ecomm_auth_service_application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    private String name;
    private String email;
    private String password;
}
