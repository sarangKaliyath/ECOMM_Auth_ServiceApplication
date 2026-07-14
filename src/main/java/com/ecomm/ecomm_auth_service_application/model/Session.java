package com.ecomm.ecomm_auth_service_application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Session extends BaseModel {
    @ManyToOne
    private User user;

    @Column(name = "token_hash", length = 64, unique = true)
    private String tokenHash;

    private Date expiresAt;

    private UUID familyId;
}
