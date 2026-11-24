package com.stratiq.backend.user.service;

import com.stratiq.backend.user.dto.request.ChangePasswordRequest;
import com.stratiq.backend.user.dto.request.UserCreateRequest;
import com.stratiq.backend.user.dto.request.UserUpdateRequest;
import com.stratiq.backend.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Page<UserResponse> findAll(Pageable pageable);
    List<UserResponse> findAllActive();
    UserResponse findById(UUID id);
    Optional<UserResponse> findByEmail(String email);
    UserResponse create(UserCreateRequest req);
    UserResponse update(UUID id, UserUpdateRequest req);
    UserResponse assignRole(UUID userId, UUID roleId);
    void changePassword(UUID userId, ChangePasswordRequest req);
    void delete(UUID id);
}
