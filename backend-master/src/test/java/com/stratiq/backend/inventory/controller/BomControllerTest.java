package com.stratiq.backend.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.inventory.dto.request.BomCreateRequest;
import com.stratiq.backend.inventory.service.BomService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = BomController.class)
class BomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BomService bomService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "inventory:read")
    void listBom_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/bom"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void listBom_filteredByItem_returnsOk() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(bomService.findByItem(itemId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/bom").param("itemId", itemId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void listBom_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/bom"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void getBom_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(bomService.findById(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/bom/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void getBom_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(bomService.findById(id)).thenThrow(new NotFoundException("BillOfMaterial", id));

        mockMvc.perform(get("/api/v1/bom/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "inventory:write")
    void createBom_returnsCreated() throws Exception {
        BomCreateRequest request = new BomCreateRequest(
            UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("2.0"), "piece", null
        );

        when(bomService.addComponent(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/bom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "inventory:delete")
    void deleteBom_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/bom/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "inventory:delete")
    void deleteBom_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("BillOfMaterial", id)).when(bomService).delete(id);

        mockMvc.perform(delete("/api/v1/bom/{id}", id))
                .andExpect(status().isNotFound());
    }
}
