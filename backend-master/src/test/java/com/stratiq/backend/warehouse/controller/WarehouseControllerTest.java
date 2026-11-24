package com.stratiq.backend.warehouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.dto.request.WarehouseCreateRequest;
import com.stratiq.backend.warehouse.dto.request.WarehouseUpdateRequest;
import com.stratiq.backend.warehouse.service.WarehouseService;
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
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = WarehouseController.class)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private WarehouseService warehouseService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void listWarehouses_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/warehouses"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void listWarehouses_filteredByCity_returnsOk() throws Exception {
        when(warehouseService.findByCity("Berlin")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/warehouses").param("city", "Berlin"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void listWarehouses_activeOnly_returnsOk() throws Exception {
        when(warehouseService.findActive()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/warehouses").param("active", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void listWarehouses_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/warehouses"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void getWarehouse_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(warehouseService.get(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/warehouses/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void getWarehouse_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(warehouseService.get(id)).thenThrow(new NotFoundException("Warehouse", id));

        mockMvc.perform(get("/api/v1/warehouses/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "warehouse:write")
    void createWarehouse_returnsCreated() throws Exception {
        WarehouseCreateRequest request = new WarehouseCreateRequest(
            "Main Warehouse", null, null, null, null, "Berlin", null, null, true
        );

        when(warehouseService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "warehouse:write")
    void updateWarehouse_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        WarehouseUpdateRequest request = new WarehouseUpdateRequest(
            "Updated Warehouse", null, null, null, null, null, null, null, null
        );

        when(warehouseService.update(eq(id), any())).thenReturn(null);

        mockMvc.perform(put("/api/v1/warehouses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warehouse:delete")
    void deleteWarehouse_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/warehouses/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "warehouse:delete")
    void deleteWarehouse_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("Warehouse", id)).when(warehouseService).delete(id);

        mockMvc.perform(delete("/api/v1/warehouses/{id}", id))
                .andExpect(status().isNotFound());
    }
}
