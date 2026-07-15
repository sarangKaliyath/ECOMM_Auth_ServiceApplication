package com.ecomm.ecomm_auth_service_application.controller;

import com.ecomm.ecomm_auth_service_application.dto.SendCodeRequestDto;
import com.ecomm.ecomm_auth_service_application.dto.VerifyCodeRequestDto;
import com.ecomm.ecomm_auth_service_application.service.VerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verify")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendCode(@RequestBody SendCodeRequestDto request) {
        verificationService.sendCode(request.getEmail(), request.getVerificationType());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> verifyCode(@RequestBody VerifyCodeRequestDto request) {
        verificationService.verifyCode(request.getEmail(), request.getCode(), request.getVerificationType());
        return ResponseEntity.noContent().build();
    }
}
