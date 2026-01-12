package com.ecomm.ecomm_auth_service_application.mapper;

import com.ecomm.ecomm_auth_service_application.dto.RoleDto;
import com.ecomm.ecomm_auth_service_application.dto.UserDto;
import com.ecomm.ecomm_auth_service_application.model.Role;
import com.ecomm.ecomm_auth_service_application.model.User;

import java.util.stream.Collectors;

public class UserMapper {

    public static User toEntity(UserDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());

        if (dto.getRoles() != null) {
            user.setRoles(
                    dto.getRoles()
                            .stream()
                            .map(UserMapper::mapRoleToEntity)
                            .collect(Collectors.toList())
            );
        }

        return user;
    }

    public static UserDto toResponse(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());

        if (user.getRoles() != null) {
            dto.setRoles(
                    user.getRoles()
                            .stream()
                            .map(UserMapper::mapRoleToDto)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }


    private static Role mapRoleToEntity(RoleDto dto) {
        Role role = new Role();
        role.setType(dto.getType());
        return role;
    }

    private static RoleDto mapRoleToDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setType(role.getType());
        return dto;
    }
}
