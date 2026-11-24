package com.stratiq.backend.user.service;

import com.stratiq.backend.user.dto.request.ChangePasswordRequest;
import com.stratiq.backend.user.dto.request.UserCreateRequest;
import com.stratiq.backend.user.dto.request.UserUpdateRequest;
import com.stratiq.backend.user.dto.response.UserResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.user.mapper.UserMapper;
import com.stratiq.backend.user.model.AppUser;
import com.stratiq.backend.user.model.Role;
import com.stratiq.backend.user.repository.AppUserRepository;
import com.stratiq.backend.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final AppUserRepository repo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAllActive() {
        return repo.findByIsActiveTrueAndDeletedAtIsNull().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByEmail(String email) {
        return repo.findByEmail(email).map(mapper::toResponse);
    }

    @Override
    public UserResponse create(UserCreateRequest req) {
        AppUser user = new AppUser();
        user.setEmail(req.email());
        // Password is BCrypt encoded. Raw passwords are never stored.
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setPreferredLang(req.preferredLang() != null ? req.preferredLang() : "de");
        user.setActive(true);
        if (req.roleId() != null) {
            Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NotFoundException("Role", req.roleId()));
            user.setRole(role);
        }
        return mapper.toResponse(repo.save(user));
    }

    @Override
    public UserResponse update(UUID id, UserUpdateRequest req) {
        AppUser user = find(id);
        if (req.email() != null) user.setEmail(req.email());
        if (req.preferredLang() != null) user.setPreferredLang(req.preferredLang());
        user.setActive(req.isActive());
        if (req.roleId() != null) {
            Role role = roleRepo.findById(req.roleId())
                .orElseThrow(() -> new NotFoundException("Role", req.roleId()));
            user.setRole(role);
        }
        return mapper.toResponse(repo.save(user));
    }

    @Override
    public UserResponse assignRole(UUID userId, UUID roleId) {
        AppUser user = find(userId);
        Role role = roleRepo.findById(roleId)
            .orElseThrow(() -> new NotFoundException("Role", roleId));
        user.setRole(role);
        return mapper.toResponse(repo.save(user));
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest req) {
        AppUser user = find(userId);
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        repo.save(user);
    }

    @Override
    public void delete(UUID id) {
        AppUser user = find(id);
        user.setDeletedAt(LocalDateTime.now());
        user.setActive(false);
        repo.save(user);
    }

    private AppUser find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("AppUser", id));
    }
}
