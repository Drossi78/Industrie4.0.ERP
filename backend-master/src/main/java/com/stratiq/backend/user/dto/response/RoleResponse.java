package com.stratiq.backend.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        LocalDateTime createdAt,
        String name,
        String description,
        List<PermissionResponse> permissions
) {}
