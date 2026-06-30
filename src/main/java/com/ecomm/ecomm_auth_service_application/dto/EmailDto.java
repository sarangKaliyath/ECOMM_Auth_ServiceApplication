package com.ecomm.ecomm_auth_service_application.dto;

import com.ecomm.ecomm_auth_service_application.model.EmailTemplate;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class EmailDto {
    private String to;
    private EmailTemplate emailTemplate;
    private Map<String, String> variables;
}
