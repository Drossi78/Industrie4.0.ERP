package com.stratiq.backend.user.mapper;

import com.stratiq.backend.user.dto.response.UserResponse;
import com.stratiq.backend.user.model.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(AppUser user) {
        // Transform user entity to response DTO; includes role details (ID and name) for authorization info.
        return new UserResponse(
            user.getId(),
            user.getCreatedAt(),
            user.getEmail(),
            user.getRole() != null ? user.getRole().getId() : null,
            user.getRole() != null ? user.getRole().getName() : null,
            user.getPreferredLang(),
            user.isActive()
        );
    }
}
