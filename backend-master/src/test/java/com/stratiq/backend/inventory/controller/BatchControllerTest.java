package com.stratiq.backend.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.inventory.dto.request.BatchCreateRequest;
import com.stratiq.backend.inventory.service.BatchService;
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
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = BatchController.class)
class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BatchService batchService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "inventory:read")
    void listBatches_returnsOk() throws Exception {
        when(batchService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/batches"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void listBatches_filteredByItem_returnsOk() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(batchService.findByItem(itemId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/batches").param("itemId", itemId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void listBatches_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/batches"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void getBatch_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(batchService.findById(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/batches/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void getBatch_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(batchService.findById(id)).thenThrow(new NotFoundException("Batch", id));

        mockMvc.perform(get("/api/v1/batches/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "inventory:write")
    void createBatch_returnsCreated() throws Exception {
        BatchCreateRequest request = new BatchCreateRequest("B-001", UUID.randomUUID(), UUID.randomUUID(), null);

        when(batchService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "inventory:delete")
    void deleteBatch_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/batches/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "inventory:delete")
    void deleteBatch_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("Batch", id)).when(batchService).delete(id);

        mockMvc.perform(delete("/api/v1/batches/{id}", id))
                .andExpect(status().isNotFound());
    }
}
