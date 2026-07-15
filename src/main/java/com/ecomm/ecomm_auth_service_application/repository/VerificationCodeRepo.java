package com.ecomm.ecomm_auth_service_application.repository;

import com.ecomm.ecomm_auth_service_application.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepo extends JpaRepository<VerificationCode, Long> {
}
