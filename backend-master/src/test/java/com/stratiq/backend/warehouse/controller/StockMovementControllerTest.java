package com.stratiq.backend.warehouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.dto.request.StockMovementCreateRequest;
import com.stratiq.backend.warehouse.service.StockMovementService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = StockMovementController.class)
class StockMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StockMovementService stockMovementService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void listStockMovements_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/stock-movements"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void listStockMovements_filteredByType_returnsOk() throws Exception {
        when(stockMovementService.findByType("IN")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/stock-movements").param("type", "IN"))
                .andExpect(status().isOk());
    }

    @Test
    void listStockMovements_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/stock-movements"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void getStockMovement_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockMovementService.get(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/stock-movements/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void getStockMovement_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockMovementService.get(id)).thenThrow(new NotFoundException("StockMovement", id));

        mockMvc.perform(get("/api/v1/stock-movements/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "warehouse:write")
    void createStockMovement_returnsCreated() throws Exception {
        StockMovementCreateRequest request = new StockMovementCreateRequest(
            "Incoming stock", 100L, "IN", UUID.randomUUID(), null, UUID.randomUUID()
        );

        when(stockMovementService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "warehouse:delete")
    void deleteStockMovement_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/stock-movements/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "warehouse:delete")
    void deleteStockMovement_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("StockMovement", id)).when(stockMovementService).delete(id);

        mockMvc.perform(delete("/api/v1/stock-movements/{id}", id))
                .andExpect(status().isNotFound());
    }
}
