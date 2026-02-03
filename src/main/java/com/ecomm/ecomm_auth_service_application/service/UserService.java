package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.dto.UserDto;
import com.ecomm.ecomm_auth_service_application.exception.UserNotFoundException;
import com.ecomm.ecomm_auth_service_application.model.User;
import com.ecomm.ecomm_auth_service_application.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.ecomm.ecomm_auth_service_application.mapper.UserMapper.toResponse;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepo userRepo;

    public UserDto getUserDetailsById(Long id) {
        Optional<User> userOptional = userRepo.findById(id);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User by id " + id + " not found");
        }

        return toResponse(userOptional.get());
    }
}
