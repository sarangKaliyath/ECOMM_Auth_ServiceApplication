package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.model.VerificationType;

public interface VerificationService {

    void sendCode(String email, VerificationType verificationType);

    // Returns a one-time password-reset token when verificationType is PASSWORD_RESET
    // (to be exchanged via AuthService.resetPassword), null for all other types.
    String verifyCode(String email, String code, VerificationType verificationType);
}
