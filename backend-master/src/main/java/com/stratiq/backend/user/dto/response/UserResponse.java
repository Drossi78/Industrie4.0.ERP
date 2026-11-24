package com.stratiq.backend.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        LocalDateTime createdAt,
        String email,
        UUID roleId,
        String roleName,
        String preferredLang,
        boolean isActive
) {}
