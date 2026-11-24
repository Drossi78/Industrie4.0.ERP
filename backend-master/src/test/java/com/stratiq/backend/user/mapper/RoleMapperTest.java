package com.stratiq.backend.user.mapper;

import com.stratiq.backend.user.dto.response.RoleResponse;
import com.stratiq.backend.user.model.Permission;
import com.stratiq.backend.user.model.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoleMapperTest {

    private final RoleMapper mapper = new RoleMapper();

    @Test
    void toResponse_withPermissions_includesPermissionList() {
        Permission perm = new Permission();
        perm.setId(UUID.randomUUID());
        perm.setName("Read Inventory");
        perm.setCodename("inventory:read");

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("Warehouse Staff");
        role.setDescription("Can read inventory");
        role.setPermissions(Set.of(perm));

        RoleResponse response = mapper.toResponse(role);

        assertThat(response.id()).isEqualTo(role.getId());
        assertThat(response.name()).isEqualTo("Warehouse Staff");
        assertThat(response.permissions()).hasSize(1);
        assertThat(response.permissions().get(0).codename()).isEqualTo("inventory:read");
    }

    @Test
    void toResponse_withNullPermissions_returnsEmptyList() {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("Empty Role");
        role.setPermissions(null);

        RoleResponse response = mapper.toResponse(role);

        assertThat(response.permissions()).isEmpty();
    }

    @Test
    void toResponse_withEmptyPermissions_returnsEmptyList() {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("Empty Role");
        role.setPermissions(Set.of());

        RoleResponse response = mapper.toResponse(role);

        assertThat(response.permissions()).isEmpty();
    }
}
