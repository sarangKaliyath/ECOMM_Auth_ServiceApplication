package com.ecomm.ecomm_auth_service_application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Session extends BaseModel {
    @ManyToOne
    private User user;

    @Column(length = 2048)
    private String token;
}
