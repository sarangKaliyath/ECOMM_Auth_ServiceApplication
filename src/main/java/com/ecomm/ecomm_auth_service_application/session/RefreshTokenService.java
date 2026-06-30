package com.ecomm.ecomm_auth_service_application.session;

import com.ecomm.ecomm_auth_service_application.dto.RefreshResponseDto;
import com.ecomm.ecomm_auth_service_application.exception.InvalidTokenException;
import com.ecomm.ecomm_auth_service_application.exception.TokenExpiredException;
import com.ecomm.ecomm_auth_service_application.jwt.JwtService;
import com.ecomm.ecomm_auth_service_application.model.Role;
import com.ecomm.ecomm_auth_service_application.model.Session;
import com.ecomm.ecomm_auth_service_application.model.State;
import com.ecomm.ecomm_auth_service_application.model.User;
import com.ecomm.ecomm_auth_service_application.repository.SessionRepo;
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
        String token = UUID.randomUUID().toString();

        Session session = new Session();
        session.setUser(user);
        session.setRefreshToken(token);
        session.setState(State.ACTIVE);
        session.setCreatedAt(new Date());
        session.setExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));

        sessionRepo.save(session);
        return token;
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

        String newRefreshToken = createRefreshToken(session.getUser());

        String accessToken = generateAccessToken(session.getUser());

        return new RefreshResponseDto(newRefreshToken, accessToken, 900L);
    }

    // ── Invalidate (logout) ───────────────────────────────────────────────────

    // Idempotent: silently ignores an already-invalid or missing token
    // so that logout is always safe to call.
    @Transactional
    public void invalidateSession(String rawToken) {
        sessionRepo.findByRefreshToken(rawToken).ifPresent(session -> {
            session.setState(State.INACTIVE);
            session.setUpdatedAt(new Date());
            sessionRepo.save(session);
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Session validateSession(String rawToken) {
        Session session = sessionRepo.findByRefreshToken(rawToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (session.getState() == State.INACTIVE) {
            throw new InvalidTokenException("Refresh token has been revoked");
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
