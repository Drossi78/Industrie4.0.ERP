package com.stratiq.backend.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.inventory.dto.request.SupplierCreateRequest;
import com.stratiq.backend.inventory.dto.request.SupplierUpdateRequest;
import com.stratiq.backend.inventory.service.SupplierService;
import com.stratiq.backend.shared.exception.NotFoundException;
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
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = SupplierController.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SupplierService supplierService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "inventory:read")
    void listSuppliers_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/suppliers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void listSuppliers_filteredByName_returnsOk() throws Exception {
        when(supplierService.search("Acme")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/suppliers").param("name", "Acme"))
                .andExpect(status().isOk());
    }

    @Test
    void listSuppliers_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/suppliers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void getSupplier_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(supplierService.get(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/suppliers/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void getSupplier_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(supplierService.get(id)).thenThrow(new NotFoundException("Supplier", id));

        mockMvc.perform(get("/api/v1/suppliers/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "inventory:write")
    void createSupplier_returnsCreated() throws Exception {
        SupplierCreateRequest request = new SupplierCreateRequest(
            "Acme Supplies", "acme@example.com", null, null,
            null, null, null, null, null, null, null
        );

        when(supplierService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "inventory:write")
    void updateSupplier_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        SupplierUpdateRequest request = new SupplierUpdateRequest(
            "Updated Acme", null, null, null, null, null, null, null, null, null, null
        );

        when(supplierService.update(eq(id), any())).thenReturn(null);

        mockMvc.perform(put("/api/v1/suppliers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:delete")
    void deleteSupplier_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/suppliers/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "inventory:delete")
    void deleteSupplier_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("Supplier", id)).when(supplierService).delete(id);

        mockMvc.perform(delete("/api/v1/suppliers/{id}", id))
                .andExpect(status().isNotFound());
    }
}
