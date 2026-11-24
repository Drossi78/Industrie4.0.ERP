package com.stratiq.backend.production.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.production.dto.request.OrderProductionStepCreateRequest;
import com.stratiq.backend.production.model.ProductionStepStatus;
import com.stratiq.backend.production.service.OrderProductionStepService;
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
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = OrderProductionStepController.class)
class OrderProductionStepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderProductionStepService opsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "production:read")
    void listOrderProductionSteps_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/order-production-steps"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "production:read")
    void listOrderProductionSteps_filteredByOrder_returnsOk() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(opsService.findByOrder(orderId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/order-production-steps").param("orderId", orderId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void listOrderProductionSteps_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/order-production-steps"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "production:read")
    void getOrderProductionStep_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(opsService.get(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/order-production-steps/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "production:read")
    void getOrderProductionStep_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(opsService.get(id)).thenThrow(new NotFoundException("OrderProductionStep", id));

        mockMvc.perform(get("/api/v1/order-production-steps/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "production:write")
    void createOrderProductionStep_returnsCreated() throws Exception {
        OrderProductionStepCreateRequest request = new OrderProductionStepCreateRequest(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            "Cutting", null, 1, ProductionStepStatus.PENDING, null
        );

        when(opsService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/order-production-steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "production:delete")
    void deleteOrderProductionStep_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/order-production-steps/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "production:delete")
    void deleteOrderProductionStep_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("OrderProductionStep", id)).when(opsService).delete(id);

        mockMvc.perform(delete("/api/v1/order-production-steps/{id}", id))
                .andExpect(status().isNotFound());
    }
}
