package com.stratiq.backend.user.service;

import com.stratiq.backend.user.dto.request.PermissionAssignRequest;
import com.stratiq.backend.user.dto.request.RoleCreateRequest;
import com.stratiq.backend.user.dto.request.RoleUpdateRequest;
import com.stratiq.backend.user.dto.response.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    List<RoleResponse> findAll();
    RoleResponse findById(UUID id);
    RoleResponse create(RoleCreateRequest req);
    RoleResponse update(UUID id, RoleUpdateRequest req);
    RoleResponse assignPermissions(UUID roleId, PermissionAssignRequest req);
    RoleResponse revokePermissions(UUID roleId, PermissionAssignRequest req);
    void delete(UUID id);
}
