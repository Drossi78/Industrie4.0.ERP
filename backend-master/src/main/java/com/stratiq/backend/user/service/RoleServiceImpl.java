package com.stratiq.backend.user.service;

import com.stratiq.backend.user.dto.request.PermissionAssignRequest;
import com.stratiq.backend.user.dto.request.RoleCreateRequest;
import com.stratiq.backend.user.dto.request.RoleUpdateRequest;
import com.stratiq.backend.user.dto.response.RoleResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.user.mapper.RoleMapper;
import com.stratiq.backend.user.model.Permission;
import com.stratiq.backend.user.model.Role;
import com.stratiq.backend.user.repository.PermissionRepository;
import com.stratiq.backend.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repo;
    private final PermissionRepository permissionRepo;
    private final RoleMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return repo.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse findById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    public RoleResponse create(RoleCreateRequest req) {
        Role role = new Role();
        role.setName(req.name());
        role.setDescription(req.description());
        return mapper.toResponse(repo.save(role));
    }

    @Override
    public RoleResponse update(UUID id, RoleUpdateRequest req) {
        Role role = find(id);
        if (req.name() != null) role.setName(req.name());
        if (req.description() != null) role.setDescription(req.description());
        return mapper.toResponse(repo.save(role));
    }

    @Override
    public RoleResponse assignPermissions(UUID roleId, PermissionAssignRequest req) {
        // Grants permissions to role. Each permission is validated for existence.
        Role role = find(roleId);
        for (UUID permId : req.permissionIds()) {
            Permission perm = permissionRepo.findById(permId)
                .orElseThrow(() -> new NotFoundException("Permission", permId));
            role.getPermissions().add(perm);
        }
        return mapper.toResponse(repo.save(role));
    }

    @Override
    public RoleResponse revokePermissions(UUID roleId, PermissionAssignRequest req) {
        // Removes permissions from role by matching IDs.
        Role role = find(roleId);
        role.getPermissions().removeIf(p -> req.permissionIds().contains(p.getId()));
        return mapper.toResponse(repo.save(role));
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("Role", id);
        repo.deleteById(id);
    }

    private Role find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Role", id));
    }
}
