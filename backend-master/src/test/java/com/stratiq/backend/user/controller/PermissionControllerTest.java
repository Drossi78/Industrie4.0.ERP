package com.stratiq.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.user.dto.request.PermissionCreateRequest;
import com.stratiq.backend.user.service.PermissionService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = PermissionController.class)
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "user:read")
    void listPermissions_returnsOk() throws Exception {
        when(permissionService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isOk());
    }

    @Test
    void listPermissions_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "admin:access")
    void createPermission_returnsCreated() throws Exception {
        PermissionCreateRequest request = new PermissionCreateRequest("Read Inventory", "inventory:read");

        when(permissionService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void createPermission_withoutAdminAccess_returns403() throws Exception {
        PermissionCreateRequest request = new PermissionCreateRequest("Read Inventory", "inventory:read");

        mockMvc.perform(post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
