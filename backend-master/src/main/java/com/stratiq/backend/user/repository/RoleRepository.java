package com.stratiq.backend.user.repository;

import com.stratiq.backend.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    // Lookup role by unique name (role assignment and permission inheritance).
    Optional<Role> findByName(String name);
}
