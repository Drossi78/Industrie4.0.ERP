package com.stratiq.backend.user.service;

import com.stratiq.backend.user.dto.request.PermissionCreateRequest;
import com.stratiq.backend.user.dto.response.PermissionResponse;
import com.stratiq.backend.user.model.Permission;
import com.stratiq.backend.user.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository repo;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> findAll() {
        return repo.findAll().stream()
            .map(p -> new PermissionResponse(p.getId(), p.getName(), p.getCodename()))
            .toList();
    }

    @Override
    public PermissionResponse create(PermissionCreateRequest req) {
        Permission perm = new Permission();
        perm.setName(req.name());
        perm.setCodename(req.codename());
        perm = repo.save(perm);
        return new PermissionResponse(perm.getId(), perm.getName(), perm.getCodename());
    }
}
