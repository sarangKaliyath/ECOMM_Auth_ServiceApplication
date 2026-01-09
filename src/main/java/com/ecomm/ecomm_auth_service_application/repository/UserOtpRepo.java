package com.ecomm.ecomm_auth_service_application.repository;

import com.ecomm.ecomm_auth_service_application.model.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOtpRepo extends JpaRepository<UserOtp, Long> {
}
