package com.stratiq.backend.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.inventory.dto.request.ItemCreateRequest;
import com.stratiq.backend.inventory.dto.request.ItemUpdateRequest;
import com.stratiq.backend.inventory.model.ItemKind;
import com.stratiq.backend.inventory.service.ItemService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "inventory:read")
    void listItems_returnsOk() throws Exception {
        when(itemService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void listItems_filteredByCategory_returnsOk() throws Exception {
        UUID categoryId = UUID.randomUUID();
        when(itemService.findByCategory(categoryId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/items").param("categoryId", categoryId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void listItems_filteredByKind_returnsOk() throws Exception {
        when(itemService.findByKind(ItemKind.OWN_PRODUCT)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/items").param("kind", "OWN_PRODUCT"))
                .andExpect(status().isOk());
    }

    @Test
    void listItems_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void getItem_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(itemService.findById(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/items/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void getItem_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(itemService.findById(id)).thenThrow(new NotFoundException("Item", id));

        mockMvc.perform(get("/api/v1/items/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "inventory:write")
    void createItem_returnsCreated() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest(
            "Laptop", ItemKind.OWN_PRODUCT, null,
            "piece", new BigDecimal("999.99"), "High-performance laptop", null
        );

        when(itemService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "inventory:write")
    void updateItem_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        ItemUpdateRequest request = new ItemUpdateRequest(
            "Updated Laptop", null, null, null, null, null, null
        );

        when(itemService.update(eq(id), any())).thenReturn(null);

        mockMvc.perform(put("/api/v1/items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // The web client updates via PATCH; the endpoint must accept it (#9).
    @Test
    @WithMockUser(authorities = "inventory:write")
    void updateItem_viaPatch_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        ItemUpdateRequest request = new ItemUpdateRequest(
            "Patched Laptop", null, null, null, null, null, null
        );

        when(itemService.update(eq(id), any())).thenReturn(null);

        mockMvc.perform(patch("/api/v1/items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:delete")
    void deleteItem_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/items/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "inventory:delete")
    void deleteItem_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("Item", id)).when(itemService).delete(id);

        mockMvc.perform(delete("/api/v1/items/{id}", id))
                .andExpect(status().isNotFound());
    }
}
