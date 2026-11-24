package com.stratiq.backend.audit.controller;

import com.stratiq.backend.audit.service.AuditService;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import com.stratiq.backend.shared.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = AuditController.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "audit:read")
    void listAuditLogs_returnsOk() throws Exception {
        when(auditService.findAll(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "audit:read")
    void listAuditLogs_filteredByUser_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        when(auditService.findByUser(eq(userId), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/audit-logs").param("userId", userId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "audit:read")
    void listAuditLogs_filteredByEntityTypeAndId_returnsOk() throws Exception {
        UUID entityId = UUID.randomUUID();
        when(auditService.findByEntity(eq("Item"), eq(entityId), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("entityType", "Item")
                        .param("entityId", entityId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void listAuditLogs_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void listAuditLogs_withoutAuditAuthority_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isForbidden());
    }
}
