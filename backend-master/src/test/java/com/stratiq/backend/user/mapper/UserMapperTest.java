package com.stratiq.backend.user.mapper;

import com.stratiq.backend.user.dto.response.UserResponse;
import com.stratiq.backend.user.model.AppUser;
import com.stratiq.backend.user.model.Role;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toResponse_withRole_includesRoleDetails() {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("Admin");

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@example.com");
        user.setRole(role);
        user.setPreferredLang("en");
        user.setActive(true);

        UserResponse response = mapper.toResponse(user);

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.email()).isEqualTo("admin@example.com");
        assertThat(response.roleId()).isEqualTo(role.getId());
        assertThat(response.roleName()).isEqualTo("Admin");
        assertThat(response.preferredLang()).isEqualTo("en");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void toResponse_withoutRole_roleFieldsAreNull() {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPreferredLang("de");

        UserResponse response = mapper.toResponse(user);

        assertThat(response.roleId()).isNull();
        assertThat(response.roleName()).isNull();
    }
}
