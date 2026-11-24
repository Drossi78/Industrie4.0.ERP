package com.stratiq.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.user.dto.request.PermissionAssignRequest;
import com.stratiq.backend.user.dto.request.RoleCreateRequest;
import com.stratiq.backend.user.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import com.stratiq.backend.shared.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "user:read")
    void listRoles_returnsOk() throws Exception {
        when(roleService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk());
    }

    @Test
    void listRoles_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void getRole_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(roleService.findById(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/roles/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void getRole_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(roleService.findById(id)).thenThrow(new NotFoundException("Role", id));

        mockMvc.perform(get("/api/v1/roles/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "admin:access")
    void createRole_returnsCreated() throws Exception {
        RoleCreateRequest request = new RoleCreateRequest("MANAGER", "Manager role");

        when(roleService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "admin:access")
    void assignPermissions_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        PermissionAssignRequest request = new PermissionAssignRequest(List.of(UUID.randomUUID()));

        when(roleService.assignPermissions(eq(id), any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/roles/{id}/permissions", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "admin:access")
    void deleteRole_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/roles/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "admin:access")
    void deleteRole_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("Role", id)).when(roleService).delete(id);

        mockMvc.perform(delete("/api/v1/roles/{id}", id))
                .andExpect(status().isNotFound());
    }
}
