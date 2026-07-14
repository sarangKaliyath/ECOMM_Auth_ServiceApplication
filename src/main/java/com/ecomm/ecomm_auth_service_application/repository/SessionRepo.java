package com.ecomm.ecomm_auth_service_application.repository;

import com.ecomm.ecomm_auth_service_application.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepo extends JpaRepository<Session, Long> {
    public Optional<Session> findByTokenHash(String tokenHash);

    // REQUIRES_NEW so this commits immediately in its own transaction: the caller
    // (validateSession, invoked from refreshAccessToken) throws a RuntimeException
    // right after this runs, which would otherwise roll back the enclosing
    // @Transactional method and undo the revocation along with it.
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Session s SET s.state = com.ecomm.ecomm_auth_service_application.model.State.INACTIVE, " +
            "s.updatedAt = CURRENT_TIMESTAMP WHERE s.familyId = :familyId " +
            "AND s.state = com.ecomm.ecomm_auth_service_application.model.State.ACTIVE")
    int revokeFamily(@Param("familyId") UUID familyId);
}
