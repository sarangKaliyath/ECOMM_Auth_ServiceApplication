package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.model.VerificationType;
import org.springframework.stereotype.Service;

@Service
public interface VerificationService {

    public void sendCode(String email, VerificationType verificationType);

    public void verifyCode(String email, String code, VerificationType verificationType);
}
