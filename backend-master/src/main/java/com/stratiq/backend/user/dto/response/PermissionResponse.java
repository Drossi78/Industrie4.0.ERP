package com.stratiq.backend.user.dto.response;

import java.util.UUID;

public record PermissionResponse(UUID id, String name, String codename) {}
