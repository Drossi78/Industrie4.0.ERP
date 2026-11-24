package com.stratiq.backend.production.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.production.dto.request.ProductionStepCreateRequest;
import com.stratiq.backend.production.service.ProductionStepService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = ProductionStepController.class)
class ProductionStepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductionStepService productionStepService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "production:read")
    void listProductionSteps_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/production-steps"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "production:read")
    void listProductionSteps_filteredByItem_returnsOk() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(productionStepService.findByItem(itemId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/production-steps").param("itemId", itemId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void listProductionSteps_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/production-steps"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "production:read")
    void getProductionStep_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(productionStepService.get(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/production-steps/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "production:read")
    void getProductionStep_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(productionStepService.get(id)).thenThrow(new NotFoundException("ProductionStep", id));

        mockMvc.perform(get("/api/v1/production-steps/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "production:write")
    void createProductionStep_returnsCreated() throws Exception {
        ProductionStepCreateRequest request = new ProductionStepCreateRequest(
            UUID.randomUUID(), "Cutting", "Cut material to size", 1, null
        );

        when(productionStepService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/production-steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "production:delete")
    void deleteProductionStep_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/production-steps/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "production:delete")
    void deleteProductionStep_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("ProductionStep", id)).when(productionStepService).delete(id);

        mockMvc.perform(delete("/api/v1/production-steps/{id}", id))
                .andExpect(status().isNotFound());
    }
}
