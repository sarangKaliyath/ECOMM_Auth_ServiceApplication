package com.ecomm.ecomm_auth_service_application.session;

import com.ecomm.ecomm_auth_service_application.dto.RefreshResponseDto;
import com.ecomm.ecomm_auth_service_application.exception.InvalidTokenException;
import com.ecomm.ecomm_auth_service_application.exception.RefreshTokenReuseException;
import com.ecomm.ecomm_auth_service_application.exception.TokenExpiredException;
import com.ecomm.ecomm_auth_service_application.jwt.JwtService;
import com.ecomm.ecomm_auth_service_application.model.Role;
import com.ecomm.ecomm_auth_service_application.model.Session;
import com.ecomm.ecomm_auth_service_application.model.State;
import com.ecomm.ecomm_auth_service_application.model.User;
import com.ecomm.ecomm_auth_service_application.repository.SessionRepo;
import com.ecomm.ecomm_auth_service_application.security.TokenHasher;
import com.ecomm.ecomm_auth_service_application.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    private final SessionRepo sessionRepo;
    private final JwtService jwtService;

    public RefreshTokenService(SessionRepo sessionRepo, JwtService jwtService) {
        this.sessionRepo = sessionRepo;
        this.jwtService = jwtService;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public String createRefreshToken(User user) {
        return createRefreshToken(user, UUID.randomUUID());
    }

    // Rotation carries the family id forward so every token descended from
    // the same login can be revoked together if reuse is ever detected.
    private String createRefreshToken(User user, UUID familyId) {
        String rawToken = UUID.randomUUID().toString();

        Session session = new Session();
        session.setUser(user);
        session.setTokenHash(TokenHasher.hash(rawToken));
        session.setFamilyId(familyId);
        session.setState(State.ACTIVE);
        session.setCreatedAt(new Date());
        session.setExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));

        sessionRepo.save(session);
        return rawToken;
    }

    // ── Refresh with rotation ─────────────────────────────────────────────────

    // Validates the incoming refresh token, rotates it (old session → INACTIVE,
    // new session created), generates a fresh access token, and returns both.
    // The controller is responsible for setting the new refresh token as a cookie.
    @Transactional
    public RefreshResponseDto refreshAccessToken(String rawToken) {
        Session session = validateSession(rawToken);

        // Rotation: invalidate old session before issuing new one
        session.setState(State.INACTIVE);
        session.setUpdatedAt(new Date());
        sessionRepo.save(session);

        String newRefreshToken = createRefreshToken(session.getUser(), session.getFamilyId());

        String accessToken = generateAccessToken(session.getUser());

        return new RefreshResponseDto(newRefreshToken, accessToken, 900L);
    }

    // ── Invalidate (logout) ───────────────────────────────────────────────────

    // Idempotent: silently ignores an already-invalid or missing token
    // so that logout is always safe to call.
    @Transactional
    public void invalidateSession(String rawToken) {
        sessionRepo.findByTokenHash(TokenHasher.hash(rawToken)).ifPresent(session -> {
            session.setState(State.INACTIVE);
            session.setUpdatedAt(new Date());
            sessionRepo.save(session);
        });
    }

    // ── Invalidate all sessions for the user (logout everywhere) ─────────────

    @Transactional
    public void logoutAllSessions(String rawToken) {
        Session session = validateSession(rawToken);
        sessionRepo.revokeAllSessionsForUser(session.getUser().getId());
    }

    // Same revocation, but keyed directly by user id — used by password reset,
    // where the caller has no raw refresh token to validate ownership through.
    @Transactional
    public void revokeAllSessionsForUser(Long userId) {
        sessionRepo.revokeAllSessionsForUser(userId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Session validateSession(String rawToken) {
        Session session = sessionRepo.findByTokenHash(TokenHasher.hash(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (session.getState() == State.INACTIVE) {
            // Already-rotated (or revoked) token presented again: treat as theft
            // and kill every session descended from the same login.
            sessionRepo.revokeFamily(session.getFamilyId());
            throw new RefreshTokenReuseException("Refresh token reuse detected; all sessions revoked");
        }

        if (session.getExpiresAt().before(new Date())) {
            throw new TokenExpiredException("Refresh token has expired");
        }

        return session;
    }

    private String generateAccessToken(User user) {
        UserPrincipal principal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getType).toList()
        );
        return jwtService.createAccessToken(principal);
    }
}
