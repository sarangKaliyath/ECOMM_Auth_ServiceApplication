package com.ecomm.ecomm_auth_service_application.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserPrincipal {

    private Long id;

    private String email;

    private List<String> roles;

}