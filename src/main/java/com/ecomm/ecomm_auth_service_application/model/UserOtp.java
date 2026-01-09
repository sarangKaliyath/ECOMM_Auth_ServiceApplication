package com.ecomm.ecomm_auth_service_application.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class UserOtp extends BaseModel {
    @ManyToOne
    private User user;

    private String otp;

    @Enumerated(EnumType.STRING)
    private OtpType otpType;

    private LocalDateTime expiryTime;
}
