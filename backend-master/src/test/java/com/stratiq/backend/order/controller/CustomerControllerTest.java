package com.stratiq.backend.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.order.dto.request.CustomerCreateRequest;
import com.stratiq.backend.order.dto.request.CustomerUpdateRequest;
import com.stratiq.backend.order.service.CustomerService;
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

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "customer:read")
    void listCustomers_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "customer:read")
    void listCustomers_filteredByName_returnsOk() throws Exception {
        when(customerService.findByName("Mustermann")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/customers").param("name", "Mustermann"))
                .andExpect(status().isOk());
    }

    @Test
    void listCustomers_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "customer:read")
    void getCustomer_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerService.get(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "customer:read")
    void getCustomer_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerService.get(id)).thenThrow(new NotFoundException("Customer", id));

        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "customer:write")
    void createCustomer_returnsCreated() throws Exception {
        CustomerCreateRequest request = new CustomerCreateRequest(
            "Mustermann GmbH", "info@mustermann.de", null, null,
            null, null, null, null, null, null, null
        );

        when(customerService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "customer:write")
    void updateCustomer_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerUpdateRequest request = new CustomerUpdateRequest(
            "Updated GmbH", null, null, null, null, null, null, null, null, null, null
        );

        when(customerService.update(eq(id), any())).thenReturn(null);

        mockMvc.perform(put("/api/v1/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "customer:delete")
    void deleteCustomer_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/customers/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "customer:delete")
    void deleteCustomer_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("Customer", id)).when(customerService).delete(id);

        mockMvc.perform(delete("/api/v1/customers/{id}", id))
                .andExpect(status().isNotFound());
    }
}
