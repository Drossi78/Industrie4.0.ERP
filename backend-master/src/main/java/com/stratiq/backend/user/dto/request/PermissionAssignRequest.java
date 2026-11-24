package com.stratiq.backend.user.dto.request;

import java.util.List;
import java.util.UUID;

public record PermissionAssignRequest(List<UUID> permissionIds) {}
