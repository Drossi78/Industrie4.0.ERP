package com.stratiq.backend.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.order.dto.request.OrderCreateRequest;
import com.stratiq.backend.order.model.OrderStatus;
import com.stratiq.backend.order.service.OrderService;
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
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "order:read")
    void listOrders_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "order:read")
    void listOrders_filteredByStatus_returnsOk() throws Exception {
        when(orderService.findByStatus(OrderStatus.DRAFT)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders").param("status", "DRAFT"))
                .andExpect(status().isOk());
    }

    @Test
    void listOrders_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "order:read")
    void getOrder_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.get(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "order:read")
    void getOrder_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.get(id)).thenThrow(new NotFoundException("Order", id));

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "order:write")
    void createOrder_returnsCreated() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest(
            UUID.randomUUID(), "ORD-001", OrderStatus.DRAFT, null,
            null, null, null, "Test order"
        );

        when(orderService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "order:write")
    void confirmOrder_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.confirm(id)).thenReturn(null);

        mockMvc.perform(post("/api/v1/orders/{id}/confirm", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "order:write")
    void cancelOrder_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.cancel(id)).thenReturn(null);

        mockMvc.perform(post("/api/v1/orders/{id}/cancel", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "order:delete")
    void deleteOrder_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/orders/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "order:delete")
    void deleteOrder_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("Order", id)).when(orderService).delete(id);

        mockMvc.perform(delete("/api/v1/orders/{id}", id))
                .andExpect(status().isNotFound());
    }
}
