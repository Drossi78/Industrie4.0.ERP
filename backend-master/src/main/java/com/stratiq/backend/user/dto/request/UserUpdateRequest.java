package com.stratiq.backend.user.dto.request;

import java.util.UUID;

public record UserUpdateRequest(
        String email,
        UUID roleId,
        String preferredLang,
        boolean isActive
) {}
