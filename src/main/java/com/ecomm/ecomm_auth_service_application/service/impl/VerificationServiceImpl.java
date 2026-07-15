package com.ecomm.ecomm_auth_service_application.service.impl;

import com.ecomm.ecomm_auth_service_application.client.KafkaClient;
import com.ecomm.ecomm_auth_service_application.dto.EmailDto;
import com.ecomm.ecomm_auth_service_application.exception.InvalidVerificationCodeException;
import com.ecomm.ecomm_auth_service_application.exception.TooManyVerificationAttemptsException;
import com.ecomm.ecomm_auth_service_application.exception.UnsupportedVerificationTypeException;
import com.ecomm.ecomm_auth_service_application.exception.UserNotFoundException;
import com.ecomm.ecomm_auth_service_application.exception.VerificationCodeExpiredException;
import com.ecomm.ecomm_auth_service_application.exception.VerificationCodeNotFoundException;
import com.ecomm.ecomm_auth_service_application.model.EmailTemplate;
import com.ecomm.ecomm_auth_service_application.model.State;
import com.ecomm.ecomm_auth_service_application.model.User;
import com.ecomm.ecomm_auth_service_application.model.VerificationCode;
import com.ecomm.ecomm_auth_service_application.model.VerificationStatus;
import com.ecomm.ecomm_auth_service_application.model.VerificationType;
import com.ecomm.ecomm_auth_service_application.repository.UserRepo;
import com.ecomm.ecomm_auth_service_application.repository.VerificationCodeRepo;
import com.ecomm.ecomm_auth_service_application.service.VerificationService;
import com.ecomm.ecomm_auth_service_application.verification.CodeGenerator;
import com.ecomm.ecomm_auth_service_application.verification.CodeHashService;
import com.ecomm.ecomm_auth_service_application.verification.VerificationCodeProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final UserRepo userRepo;
    private final VerificationCodeRepo verificationCodeRepo;
    private final CodeGenerator codeGenerator;
    private final CodeHashService codeHashService;
    private final VerificationCodeProperties verificationCodeProperties;
    private final KafkaClient kafkaClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void sendCode(String email, VerificationType verificationType) {
        rejectPhoneVerification(verificationType);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

        long expiryMinutes = verificationCodeProperties.expiryMinutesFor(verificationType);

        expireExistingPendingCodes(email, verificationType);

        String rawCode = codeGenerator.generate();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUser(user);
        verificationCode.setEmail(email);
        verificationCode.setVerificationType(verificationType);
        verificationCode.setVerificationStatus(VerificationStatus.PENDING);
        verificationCode.setCodeHash(codeHashService.hash(rawCode));
        verificationCode.setAttempts(0);
        verificationCode.setState(State.ACTIVE);
        verificationCode.setExpiryTime(LocalDateTime.now().plusMinutes(expiryMinutes));
        verificationCode.setCreatedAt(new Date());
        verificationCode.setUpdatedAt(new Date());

        verificationCodeRepo.save(verificationCode);

        sendVerificationEmail(email, rawCode, expiryMinutes);
    }

    @Override
    @Transactional
    public void verifyCode(String email, String code, VerificationType verificationType) {
        rejectPhoneVerification(verificationType);

        VerificationCode verificationCode = verificationCodeRepo
                .findTopByEmailAndVerificationTypeAndVerificationStatusOrderByCreatedAtDesc(
                        email, verificationType, VerificationStatus.PENDING)
                .orElseThrow(() -> new VerificationCodeNotFoundException(
                        "No active verification code found for " + email));

        if (verificationCode.getExpiryTime().isBefore(LocalDateTime.now())) {
            verificationCode.setVerificationStatus(VerificationStatus.EXPIRED);
            verificationCode.setUpdatedAt(new Date());
            verificationCodeRepo.save(verificationCode);
            throw new VerificationCodeExpiredException("Verification code has expired");
        }

        if (verificationCode.getAttempts() >= verificationCodeProperties.getMaxAttempts()) {
            verificationCode.setVerificationStatus(VerificationStatus.FAILED);
            verificationCode.setUpdatedAt(new Date());
            verificationCodeRepo.save(verificationCode);
            throw new TooManyVerificationAttemptsException("Too many failed verification attempts");
        }

        if (!codeHashService.matches(code, verificationCode.getCodeHash())) {
            verificationCode.setAttempts(verificationCode.getAttempts() + 1);
            verificationCode.setUpdatedAt(new Date());
            verificationCodeRepo.save(verificationCode);
            throw new InvalidVerificationCodeException("Invalid verification code");
        }

        verificationCode.setVerificationStatus(VerificationStatus.VERIFIED);
        verificationCode.setUsedAt(LocalDateTime.now());
        verificationCode.setUpdatedAt(new Date());
        verificationCodeRepo.save(verificationCode);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void rejectPhoneVerification(VerificationType verificationType) {
        if (verificationType == VerificationType.PHONE_VERIFICATION) {
            throw new UnsupportedVerificationTypeException("Phone verification is not supported");
        }
    }

    private void expireExistingPendingCodes(String email, VerificationType verificationType) {
        List<VerificationCode> pending = verificationCodeRepo
                .findAllByEmailAndVerificationTypeAndVerificationStatus(
                        email, verificationType, VerificationStatus.PENDING);

        for (VerificationCode code : pending) {
            code.setVerificationStatus(VerificationStatus.EXPIRED);
            code.setUpdatedAt(new Date());
        }
        verificationCodeRepo.saveAll(pending);
    }

    private void sendVerificationEmail(String email, String rawCode, long expiryMinutes) {
        EmailDto emailDto = new EmailDto();
        emailDto.setTo(email);
        emailDto.setEmailTemplate(EmailTemplate.PASSWORD_RESET);
        emailDto.setVariables(Map.of(
                "code", rawCode,
                "expiryMinutes", String.valueOf(expiryMinutes)));

        try {
            kafkaClient.sendMessage("email", objectMapper.writeValueAsString(emailDto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
