package com.ecomm.ecomm_auth_service_application.verification;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CodeGenerator {
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
