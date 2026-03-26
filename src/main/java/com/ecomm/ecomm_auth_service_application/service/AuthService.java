package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.client.KafkaClient;
import com.ecomm.ecomm_auth_service_application.dto.*;
import com.ecomm.ecomm_auth_service_application.exception.*;
import com.ecomm.ecomm_auth_service_application.model.Role;
import com.ecomm.ecomm_auth_service_application.model.Session;
import com.ecomm.ecomm_auth_service_application.model.State;
import com.ecomm.ecomm_auth_service_application.model.User;
import com.ecomm.ecomm_auth_service_application.repository.RoleRepo;
import com.ecomm.ecomm_auth_service_application.repository.SessionRepo;
import com.ecomm.ecomm_auth_service_application.repository.UserRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ecomm.ecomm_auth_service_application.mapper.UserMapper.toResponse;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private SecretKey secretKey;

    @Autowired
    private SessionRepo sessionRepo;

    @Autowired
    private KafkaClient kafkaClient;

    @Autowired
    private ObjectMapper objectMapper;

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
        emailDto.setSubject("Welcome to Ecommerce App");
        emailDto.setBody("Welcome to Ecommerce App " + signupRequestDto.getName() + ", " + "Your account has been created successfully!");

        try {
            kafkaClient.sendMessage("signup", objectMapper.writeValueAsString(emailDto));
            return toResponse(userRepo.save(user));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Pair<LoginResponseDto, String> login(LoginRequestDto loginRequestDto) {
        User user = validateUser(loginRequestDto);

        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return new Pair<>(new LoginResponseDto(accessToken), refreshToken);
    }


    public void validateAccessToken(String token) {
        try {
            JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
            jwtParser.parseSignedClaims(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new TokenExpiredException("Access token expired");
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid access token");
        }
    }

    // ******************* Helpers *******************

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

    private String createAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roles",
                user.getRoles().stream().map(Role::getType).toList()
        );

        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiryAt = new Date(now + TimeUnit.MINUTES.toMillis(15));

        return Jwts.builder()
                .subject(user.getId().toString())
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiryAt)
                .issuer("ecommerce-auth-service")
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact()
                .trim();
    }

    private String createRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();

        Session session = new Session();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setState(State.ACTIVE);
        session.setCreatedAt(new Date());

        session.setExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));

        sessionRepo.save(session);

        return refreshToken;
    }
}