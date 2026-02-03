package com.ecomm.ecomm_auth_service_application.controller;

import com.ecomm.ecomm_auth_service_application.dto.UserDto;
import com.ecomm.ecomm_auth_service_application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserDetailsById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserDetailsById(id));
    }
}
