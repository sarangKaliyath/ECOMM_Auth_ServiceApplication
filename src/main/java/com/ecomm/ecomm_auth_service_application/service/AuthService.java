package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.client.KafkaClient;
import com.ecomm.ecomm_auth_service_application.dto.*;
import com.ecomm.ecomm_auth_service_application.dto.kafka.UserCreatedEvent;
import com.ecomm.ecomm_auth_service_application.exception.*;
import com.ecomm.ecomm_auth_service_application.jwt.JwtService;
import com.ecomm.ecomm_auth_service_application.model.*;
import com.ecomm.ecomm_auth_service_application.repository.RoleRepo;
import com.ecomm.ecomm_auth_service_application.repository.UserRepo;
import com.ecomm.ecomm_auth_service_application.session.RefreshTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.ecomm.ecomm_auth_service_application.mapper.UserMapper.toResponse;

@Service
public class AuthService implements IAuthService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final KafkaClient kafkaClient;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepo userRepo,
            RoleRepo roleRepo,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            KafkaClient kafkaClient,
            ObjectMapper objectMapper,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.kafkaClient = kafkaClient;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public UserDto signup(SignupRequestDto signupRequestDto) {
        Optional<User> userOptional = userRepo.findByEmail(signupRequestDto.getEmail());

        if (userOptional.isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = new User();
        user.setName(signupRequestDto.getName());
        user.setEmail(signupRequestDto.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(signupRequestDto.getPassword()));
        user.setState(State.ACTIVE);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        Role role;
        Optional<Role> roleOptional = roleRepo.findByType("DEFAULT");

        if (roleOptional.isEmpty()) {
            role = new Role();
            role.setType("DEFAULT");
            role.setCreatedAt(new Date());
            role.setState(State.ACTIVE);
            roleRepo.save(role);
        } else {
            role = roleOptional.get();
        }

        List<Role> rolesList = new ArrayList<>();
        rolesList.add(role);
        user.setRoles(rolesList);

        EmailDto emailDto = new EmailDto();
        emailDto.setTo(signupRequestDto.getEmail());
        emailDto.setEmailTemplate(EmailTemplate.SIGNUP_WELCOME);
        emailDto.setVariables(Map.of("name", signupRequestDto.getName()));

        String[] parts = signupRequestDto.getName().trim().split("\\s+", 2);

        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";

        User savedUser = userRepo.save(user);

        UserCreatedEvent profileEvent = new UserCreatedEvent();
        profileEvent.setAuthUserId(savedUser.getId());
        profileEvent.setFirstName(firstName);
        profileEvent.setLastName(lastName);

        try {
            kafkaClient.sendMessage("signup", objectMapper.writeValueAsString(emailDto));
            kafkaClient.sendMessage("user-created", objectMapper.writeValueAsString(profileEvent));
            return toResponse(savedUser);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Returns only the refresh token. Access tokens are issued exclusively
    // by POST /auth/refresh so all login methods share the same token-issuance path.
    @Override
    public String login(LoginRequestDto loginRequestDto) {
        User user = validateUser(loginRequestDto);
        return refreshTokenService.createRefreshToken(user);
    }

    @Override
    public void validateAccessToken(String token) {
        try {
            jwtService.validateAccessToken(token);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Access token expired");
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid access token");
        }
    }

    @Override
    public RefreshResponseDto refresh(String rawRefreshToken) {
        return refreshTokenService.refreshAccessToken(rawRefreshToken);
    }

    @Override
    public void logout(String rawRefreshToken) {
        refreshTokenService.invalidateSession(rawRefreshToken);
    }

    @Override
    public void logoutAll(String rawRefreshToken) {
        refreshTokenService.logoutAllSessions(rawRefreshToken);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User validateUser(LoginRequestDto loginRequestDto) {
        Optional<User> userOptional = userRepo.findByEmail(loginRequestDto.getEmail());

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User with email " + loginRequestDto.getEmail() + " not found");
        }

        User user = userOptional.get();

        if (user.getState() == State.INACTIVE) {
            throw new UserInactiveException("User is inactive");
        }

        if (!bCryptPasswordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException("Incorrect password");
        }

        return user;
    }
}
