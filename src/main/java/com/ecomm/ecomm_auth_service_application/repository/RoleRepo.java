package com.ecomm.ecomm_auth_service_application.repository;

import com.ecomm.ecomm_auth_service_application.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {
}
