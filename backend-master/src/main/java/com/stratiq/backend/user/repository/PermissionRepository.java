package com.stratiq.backend.user.repository;

import com.stratiq.backend.user.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    // Lookup permission by unique codename (e.g., "MANAGE_ORDERS", "VIEW_REPORTS" for authority checking).
    Optional<Permission> findByCodename(String codename);
}
