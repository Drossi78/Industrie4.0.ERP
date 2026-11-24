package com.stratiq.backend.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.inventory.dto.request.CategoryCreateRequest;
import com.stratiq.backend.inventory.service.CategoryService;
import com.stratiq.backend.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import com.stratiq.backend.shared.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
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
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "inventory:read")
    void listCategories_returnsOk() throws Exception {
        when(categoryService.list(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void listCategories_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void getCategory_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.get(id)).thenReturn(null);

        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "inventory:read")
    void getCategory_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.get(id)).thenThrow(new NotFoundException("Category", id));

        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "inventory:write")
    void createCategory_returnsCreated() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("Electronics", "Electronic items");

        when(categoryService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "inventory:delete")
    void deleteCategory_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "inventory:delete")
    void deleteCategory_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("Category", id)).when(categoryService).delete(id);

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
                .andExpect(status().isNotFound());
    }
}
