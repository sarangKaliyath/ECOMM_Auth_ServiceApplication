package com.ecomm.ecomm_auth_service_application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String createdAt;
    private String updatedAt;

    @Enumerated(EnumType.STRING)
    private State state;
}
