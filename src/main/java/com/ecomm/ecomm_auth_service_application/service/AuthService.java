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

    public Pair<User, String> login(LoginRequestDto loginRequestDto) {
        Optional<User> userOptional = userRepo.findByEmail(loginRequestDto.getEmail());

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User with email " + loginRequestDto.getEmail() + " not found");
        }

        User user = userOptional.get();

        if (!bCryptPasswordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException("Incorrect password");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("access", user.getRoles().stream().map(Role::getType).toList());

        Long now = System.currentTimeMillis();

        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + TimeUnit.MINUTES.toMillis(15)))
                .issuer("curr_org")
                .signWith(secretKey)
                .compact()
                .trim();

        Session session = new Session();

        session.setUser(user);
        session.setToken(token);
        session.setCreatedAt(new Date(now));
        session.setState(State.ACTIVE);
        sessionRepo.save(session);

        return new Pair<>(user, token);
    }

    public void validateToken(String token) {

        Optional<Session> sessionOptional = sessionRepo.findByToken(token);

        if (sessionOptional.isEmpty() || sessionOptional.get().getState() == State.INACTIVE) {
            throw new InvalidTokenException("Invalid token, Please login again!");
        }

        Session session = sessionOptional.get();

        JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

        long expirationTime = claims.getExpiration().getTime();
        long currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis > expirationTime) {
            session.setState(State.INACTIVE);
            session.setUpdatedAt(new Date());
            sessionRepo.save(session);
            throw new TokenExpiredException("Token has expired, Please login again!");
        }
    }
}