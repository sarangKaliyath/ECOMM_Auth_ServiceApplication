package com.ecomm.ecomm_auth_service_application.verification;

import com.ecomm.ecomm_auth_service_application.exception.UnsupportedVerificationTypeException;
import com.ecomm.ecomm_auth_service_application.model.VerificationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VerificationCodeProperties {

    @Value("${code.length}")
    private int codeLength;

    @Value("${code.expiry.login}")
    private long loginExpiryMinutes;

    @Value("${code.expiry.password-reset}")
    private long passwordResetExpiryMinutes;

    @Value("${code.expiry.email-verification}")
    private long emailVerificationExpiryMinutes;

    @Value("${code.max-attempts}")
    private int maxAttempts;

    public int getCodeLength() {
        return codeLength;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long expiryMinutesFor(VerificationType verificationType) {
        return switch (verificationType) {
            case LOGIN -> loginExpiryMinutes;
            case PASSWORD_RESET -> passwordResetExpiryMinutes;
            case EMAIL_VERIFICATION -> emailVerificationExpiryMinutes;
            case PHONE_VERIFICATION -> throw new UnsupportedVerificationTypeException(
                    "Phone verification is not supported");
        };
    }
}
