package com.stratiq.backend.user.repository;

import com.stratiq.backend.user.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    // Email-based user lookup (Spring Security authentication and login verification).
    Optional<AppUser> findByEmail(String email);
    // Soft-delete filter: returns only active (non-deleted) users.
    List<AppUser> findByDeletedAtIsNull();
    List<AppUser> findByIsActiveTrueAndDeletedAtIsNull();
    // Find all users assigned to a specific role (role membership query).
    List<AppUser> findByRoleId(UUID roleId);
    // Get a specific user by ID only if not soft-deleted (safe user detail retrieval).
    Optional<AppUser> findByIdAndDeletedAtIsNull(UUID id);
}
