package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.dto.OAuthLoginResult;
import com.ecomm.ecomm_auth_service_application.jwt.JwtService;
import com.ecomm.ecomm_auth_service_application.model.Role;
import com.ecomm.ecomm_auth_service_application.model.State;
import com.ecomm.ecomm_auth_service_application.model.User;
import com.ecomm.ecomm_auth_service_application.oauth.OAuthUserInfo;
import com.ecomm.ecomm_auth_service_application.repository.RoleRepo;
import com.ecomm.ecomm_auth_service_application.repository.UserRepo;
import com.ecomm.ecomm_auth_service_application.security.UserPrincipal;
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
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public GoogleOAuthService(
            UserRepo userRepo,
            RoleRepo roleRepo,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.jwtService = jwtService;
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
            return generateTokens(existingUser.get(), false);
        }

        User newUser = createOAuthUser(userInfo);
        return generateTokens(newUser, true);
    }

    // ── Token generation — mirrors AuthService.login() ────────────────────────

    private OAuthLoginResult generateTokens(User user, boolean isNewUser) {
        UserPrincipal principal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getRoles()
                        .stream()
                        .map(Role::getType)
                        .toList()
        );

        String accessToken  = jwtService.createAccessToken(principal);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new OAuthLoginResult(accessToken, refreshToken, isNewUser);
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

    // ── Role helper — replicates the find-or-create pattern in AuthService ────

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
