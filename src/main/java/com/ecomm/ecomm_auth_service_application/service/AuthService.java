package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.dto.*;
import com.ecomm.ecomm_auth_service_application.exception.UserAlreadyExistsException;
import com.ecomm.ecomm_auth_service_application.model.Role;
import com.ecomm.ecomm_auth_service_application.model.State;
import com.ecomm.ecomm_auth_service_application.model.User;
import com.ecomm.ecomm_auth_service_application.repository.RoleRepo;
import com.ecomm.ecomm_auth_service_application.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.ecomm.ecomm_auth_service_application.mapper.UserMapper.toResponse;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

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

        return toResponse(userRepo.save(user));
    }

    public UserDto login(LoginRequestDto loginRequestDto) {
        return null;
    }
}
