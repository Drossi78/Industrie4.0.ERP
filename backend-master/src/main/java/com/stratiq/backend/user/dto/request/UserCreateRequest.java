package com.stratiq.backend.user.dto.request;

import java.util.UUID;

public record UserCreateRequest(
        String email,
        String password,
        UUID roleId,
        String preferredLang
) {}
