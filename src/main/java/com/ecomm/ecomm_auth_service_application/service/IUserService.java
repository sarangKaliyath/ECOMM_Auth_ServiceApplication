package com.ecomm.ecomm_auth_service_application.service;

import com.ecomm.ecomm_auth_service_application.dto.UserDto;

public interface IUserService {
    UserDto getUserDetailsById(Long id);
}
