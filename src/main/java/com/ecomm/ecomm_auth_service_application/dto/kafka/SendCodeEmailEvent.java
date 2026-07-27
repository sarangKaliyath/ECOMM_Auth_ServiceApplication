package com.ecomm.ecomm_auth_service_application.dto.kafka;

import com.ecomm.ecomm_auth_service_application.model.VerificationType;

public class SendCodeEmailEvent {
    private String email;
    private String code;
}
