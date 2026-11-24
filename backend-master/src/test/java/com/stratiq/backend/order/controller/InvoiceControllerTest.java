package com.stratiq.backend.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.order.dto.request.InvoiceCreateRequest;
import com.stratiq.backend.order.model.InvoiceStatus;
import com.stratiq.backend.order.service.InvoiceService;
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
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = InvoiceController.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private InvoiceService invoiceService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "order:read")
    void listInvoices_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "order:read")
    void listInvoices_filteredByStatus_returnsOk() throws Exception {
        when(invoiceService.findByStatus(InvoiceStatus.ISSUED)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/invoices").param("status", "ISSUED"))
                .andExpect(status().isOk());
    }

    @Test
    void listInvoices_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "order:read")
    void getInvoice_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(invoiceService.get(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/invoices/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "order:read")
    void getInvoice_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(invoiceService.get(id)).thenThrow(new NotFoundException("Invoice", id));

        mockMvc.perform(get("/api/v1/invoices/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "order:write")
    void createInvoice_returnsCreated() throws Exception {
        InvoiceCreateRequest request = new InvoiceCreateRequest(
            UUID.randomUUID(), InvoiceStatus.ISSUED, null, null, null
        );

        when(invoiceService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "order:delete")
    void deleteInvoice_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/invoices/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "order:delete")
    void deleteInvoice_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("Invoice", id)).when(invoiceService).delete(id);

        mockMvc.perform(delete("/api/v1/invoices/{id}", id))
                .andExpect(status().isNotFound());
    }
}
