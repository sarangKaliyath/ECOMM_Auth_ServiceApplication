package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.model.VerificationType;

public interface VerificationService {

    void sendCode(String email, VerificationType verificationType);

    void verifyCode(String email, String code, VerificationType verificationType);
}
