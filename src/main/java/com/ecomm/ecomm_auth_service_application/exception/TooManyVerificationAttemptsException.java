package com.ecomm.ecomm_auth_service_application.exception;

public class TooManyVerificationAttemptsException extends RuntimeException {
    public TooManyVerificationAttemptsException(String message) {
        super(message);
    }
}
