package com.ecomm.ecomm_auth_service_application.service.impl;

import com.ecomm.ecomm_auth_service_application.dto.OAuthLoginResult;
import com.ecomm.ecomm_auth_service_application.model.Role;
import com.ecomm.ecomm_auth_service_application.model.State;
import com.ecomm.ecomm_auth_service_application.model.User;
import com.ecomm.ecomm_auth_service_application.oauth.OAuthUserInfo;
import com.ecomm.ecomm_auth_service_application.repository.RoleRepo;
import com.ecomm.ecomm_auth_service_application.repository.UserRepo;
import com.ecomm.ecomm_auth_service_application.session.RefreshTokenService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GoogleOAuthService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final RefreshTokenService refreshTokenService;

    public GoogleOAuthService(
            UserRepo userRepo,
            RoleRepo roleRepo,
            RefreshTokenService refreshTokenService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.refreshTokenService = refreshTokenService;
    }

    // ── Public entry point ────────────────────────────────────────────────────

    public OAuthLoginResult processLogin(OidcUser oidcUser) {
        OAuthUserInfo userInfo = toOAuthUserInfo(oidcUser);
        return processOAuthLogin(userInfo);
    }

    // ── Core login logic ──────────────────────────────────────────────────────

    private OAuthLoginResult processOAuthLogin(OAuthUserInfo userInfo) {
        Optional<User> existingUser = userRepo.findByEmail(userInfo.email());

        if (existingUser.isPresent()) {
            return createSession(existingUser.get(), false);
        }

        User newUser = createOAuthUser(userInfo);
        return createSession(newUser, true);
    }

    // ── Session creation — access token is issued later via POST /auth/refresh ─

    private OAuthLoginResult createSession(User user, boolean isNewUser) {
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new OAuthLoginResult(refreshToken, isNewUser);
    }

    // ── User creation — mirrors AuthService.signup() without Kafka ────────────

    private User createOAuthUser(OAuthUserInfo userInfo) {
        Role role = findOrCreateDefaultRole();

        User user = new User();
        user.setName(userInfo.name());
        user.setEmail(userInfo.email());
        user.setPassword(null);
        user.setState(State.ACTIVE);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setRoles(new ArrayList<>(List.of(role)));

        return userRepo.save(user);
    }

    // ── Role helper ───────────────────────────────────────────────────────────

    private Role findOrCreateDefaultRole() {
        return roleRepo.findByType("DEFAULT")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setType("DEFAULT");
                    role.setState(State.ACTIVE);
                    role.setCreatedAt(new Date());
                    role.setUpdatedAt(new Date());
                    return roleRepo.save(role);
                });
    }

    // ── Provider normalisation ────────────────────────────────────────────────

    private OAuthUserInfo toOAuthUserInfo(OidcUser oidcUser) {
        return new OAuthUserInfo(
                oidcUser.getEmail(),
                oidcUser.getFullName(),
                "google"
        );
    }
}
