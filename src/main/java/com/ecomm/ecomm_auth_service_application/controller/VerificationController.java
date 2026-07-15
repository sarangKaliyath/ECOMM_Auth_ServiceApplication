package com.ecomm.ecomm_auth_service_application.controller;

import com.ecomm.ecomm_auth_service_application.dto.SendCodeRequestDto;
import com.ecomm.ecomm_auth_service_application.dto.VerifyCodeRequestDto;
import com.ecomm.ecomm_auth_service_application.dto.VerifyCodeResponseDto;
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

    // Returns 200 with a one-time resetToken when verificationType is PASSWORD_RESET
    // (exchange it via POST /auth/reset-password); 204 for all other types.
    @PostMapping("/confirm")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequestDto request) {
        String resetToken = verificationService.verifyCode(
                request.getEmail(), request.getCode(), request.getVerificationType());

        if (resetToken != null) {
            return ResponseEntity.ok(new VerifyCodeResponseDto(resetToken));
        }
        return ResponseEntity.noContent().build();
    }
}
