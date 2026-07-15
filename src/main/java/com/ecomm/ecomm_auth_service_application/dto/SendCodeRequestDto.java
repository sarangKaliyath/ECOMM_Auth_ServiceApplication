package com.ecomm.ecomm_auth_service_application.dto;

import com.ecomm.ecomm_auth_service_application.model.VerificationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendCodeRequestDto {
    private String email;
    private VerificationType verificationType;
}
