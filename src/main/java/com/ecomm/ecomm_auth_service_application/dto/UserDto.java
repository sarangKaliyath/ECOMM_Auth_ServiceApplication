package com.ecomm.ecomm_auth_service_application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private List<RoleDto> roles = new ArrayList<>();
}
