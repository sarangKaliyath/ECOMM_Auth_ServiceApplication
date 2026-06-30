package com.ecomm.ecomm_auth_service_application.exception;

public class UserInactiveException extends RuntimeException {
  public UserInactiveException(String message) {
    super(message);
  }
}
