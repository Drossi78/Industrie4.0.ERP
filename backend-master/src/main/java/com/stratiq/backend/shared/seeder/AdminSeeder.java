package com.stratiq.backend.shared.seeder;

import com.stratiq.backend.user.model.AppUser;
import com.stratiq.backend.user.model.Permission;
import com.stratiq.backend.user.model.PermissionCode;
import com.stratiq.backend.user.model.Role;
import com.stratiq.backend.user.repository.AppUserRepository;
import com.stratiq.backend.user.repository.PermissionRepository;
import com.stratiq.backend.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("default")
@Order(1)
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {
    // Runs on app startup (default profile only).
    // Idempotent: safe to run multiple times — only creates missing entries.

    private final AppUserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PermissionRepository permissionRepo;
    private final PasswordEncoder passwordEncoder;

    // All permissions in the system: module:action. Source of truth is the PermissionCode enum.
    private static final Map<String, String> ALL_PERMISSIONS = Arrays.stream(PermissionCode.values())
        .collect(Collectors.toMap(PermissionCode::getCodename, PermissionCode::getDescription));

    // Which permissions each role gets (kept in sync with the demo seed data:
    // ADMIN = everything, EMPLOYEE = day-to-day operations without deletes,
    // ACCOUNTING = orders/invoices and audit trail, no warehouse/production)
    private static final Map<String, Set<String>> ROLE_PERMISSIONS = Map.of(
        "ADMIN", ALL_PERMISSIONS.keySet(),

        "EMPLOYEE", Set.of(
            "inventory:read",  "inventory:write",
            "order:read",      "order:write",
            "customer:read",   "customer:write",
            "warehouse:read",  "warehouse:write",
            "production:read", "production:write"
        ),

        "ACCOUNTING", Set.of(
            "inventory:read",
            "order:read", "order:write",
            "customer:read",
            "audit:read"
        )
    );

    private static final Map<String, String> ROLE_DESCRIPTIONS = Map.of(
        "ADMIN",      "Full system access",
        "EMPLOYEE",   "Day-to-day operations: inventory, orders, customers, warehouse and production",
        "ACCOUNTING", "Orders, invoices and audit trail; no warehouse or production access"
    );

    @Override
    public void run(String @NonNull ... args) {
        try {
            seedPermissions();
            seedRoles();
            seedAdminUser();
        } catch (Exception e) {
            log.warn("AdminSeeder failed (tables may not exist yet): {}", e.getMessage());
        }
    }

    private void seedPermissions() {
        ALL_PERMISSIONS.forEach((codename, name) ->
            permissionRepo.findByCodename(codename).orElseGet(() -> {
                Permission p = new Permission();
                p.setCodename(codename);
                p.setName(name);
                Permission saved = permissionRepo.save(p);
                log.info("Permission created: {}", codename);
                return saved;
            })
        );
    }

    private void seedRoles() {
        // Build a lookup map of all persisted permissions
        Map<String, Permission> permMap = permissionRepo.findAll().stream()
            .collect(Collectors.toMap(Permission::getCodename, p -> p));

        ROLE_PERMISSIONS.forEach((roleName, permCodenames) -> {
            Role role = roleRepo.findByName(roleName).orElseGet(() -> {
                Role r = new Role();
                r.setName(roleName);
                r.setDescription(ROLE_DESCRIPTIONS.get(roleName));
                return r;
            });

            Set<Permission> perms = permCodenames.stream()
                .map(permMap::get)
                .collect(Collectors.toSet());
            role.setPermissions(perms);
            roleRepo.save(role);
            log.info("Role seeded: {} ({} permissions)", roleName, perms.size());
        });
    }

    private void seedAdminUser() {
        if (userRepo.findByEmail("admin@stratiq.com").isPresent()) {
            log.info("Admin user already exists, skipping");
            return;
        }
        Role adminRole = roleRepo.findByName("ADMIN")
            .orElseThrow(() -> new IllegalStateException("ADMIN role not found after seeding"));

        AppUser admin = new AppUser();
        admin.setEmail("admin@stratiq.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(adminRole);
        admin.setPreferredLang("de");
        admin.setActive(true);
        userRepo.save(admin);
        log.info("Admin user created: admin@stratiq.com");
    }
}
