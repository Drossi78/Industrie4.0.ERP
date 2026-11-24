package com.stratiq.backend.user.mapper;

import com.stratiq.backend.user.dto.response.PermissionResponse;
import com.stratiq.backend.user.dto.response.RoleResponse;
import com.stratiq.backend.user.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleMapper {

    public RoleResponse toResponse(Role role) {
        // Transform role entity to response DTO; includes all associated permissions (authority details).
        List<PermissionResponse> permissions = role.getPermissions() == null ? List.of() :
            role.getPermissions().stream()
                .map(p -> new PermissionResponse(p.getId(), p.getName(), p.getCodename()))
                .toList();
        return new RoleResponse(
            role.getId(),
            role.getCreatedAt(),
            role.getName(),
            role.getDescription(),
            permissions
        );
    }
}
