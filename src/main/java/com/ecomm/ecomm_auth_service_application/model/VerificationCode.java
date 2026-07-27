package com.ecomm.ecomm_auth_service_application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class VerificationCode extends BaseModel {
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String codeHash;

    @Enumerated(EnumType.STRING)
    private VerificationType verificationType;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    private String email;

    private String phoneNumber;

    private Integer attempts;

    private LocalDateTime expiryTime;

    private LocalDateTime usedAt;

    // Only set once verifyCode succeeds for a PASSWORD_RESET code; nulled out
    // again after the token is consumed by AuthService.resetPassword so it's single-use.
    private String resetTokenHash;

    private LocalDateTime resetTokenExpiresAt;
}
