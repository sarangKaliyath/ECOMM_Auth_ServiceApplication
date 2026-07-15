package com.ecomm.ecomm_auth_service_application.repository;

import com.ecomm.ecomm_auth_service_application.model.VerificationCode;
import com.ecomm.ecomm_auth_service_application.model.VerificationStatus;
import com.ecomm.ecomm_auth_service_application.model.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationCodeRepo extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findTopByEmailAndVerificationTypeAndVerificationStatusOrderByCreatedAtDesc(
            String email, VerificationType verificationType, VerificationStatus verificationStatus);

    List<VerificationCode> findAllByEmailAndVerificationTypeAndVerificationStatus(
            String email, VerificationType verificationType, VerificationStatus verificationStatus);

    Optional<VerificationCode> findByResetTokenHash(String resetTokenHash);
}
