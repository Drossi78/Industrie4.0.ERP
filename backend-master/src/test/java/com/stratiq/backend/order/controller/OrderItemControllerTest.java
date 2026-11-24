package com.stratiq.backend.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.order.dto.request.OrderItemCreateRequest;
import com.stratiq.backend.order.service.OrderItemService;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.shared.model.Unit;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = OrderItemController.class)
class OrderItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderItemService orderItemService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "order:read")
    void listOrderItems_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/order-items"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "order:read")
    void listOrderItems_filteredByOrder_returnsOk() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderItemService.findByOrder(orderId)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/order-items").param("orderId", orderId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void listOrderItems_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/order-items"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "order:read")
    void getOrderItem_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderItemService.get(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/order-items/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "order:read")
    void getOrderItem_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderItemService.get(id)).thenThrow(new NotFoundException("OrderItem", id));

        mockMvc.perform(get("/api/v1/order-items/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "order:write")
    void createOrderItem_returnsCreated() throws Exception {
        OrderItemCreateRequest request = new OrderItemCreateRequest(
            UUID.randomUUID(), UUID.randomUUID(), "Widget A", Unit.piece,
            new BigDecimal("5"), new BigDecimal("100.00"), null, null
        );

        when(orderItemService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "order:delete")
    void deleteOrderItem_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/order-items/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "order:delete")
    void deleteOrderItem_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("OrderItem", id)).when(orderItemService).delete(id);

        mockMvc.perform(delete("/api/v1/order-items/{id}", id))
                .andExpect(status().isNotFound());
    }
}
