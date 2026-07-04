package com.ecomm.ecomm_auth_service_application.dto.kafka;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreatedEvent {
    private Long authUserId;
    private String firstName;
    private String lastName;
}
