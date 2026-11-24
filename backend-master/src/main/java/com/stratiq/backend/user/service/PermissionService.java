package com.stratiq.backend.user.service;

import com.stratiq.backend.user.dto.request.PermissionCreateRequest;
import com.stratiq.backend.user.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    List<PermissionResponse> findAll();
    PermissionResponse create(PermissionCreateRequest req);
}
