package com.stratiq.backend.warehouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.AppUserDetailsService;
import com.stratiq.backend.auth.JwtService;
import com.stratiq.backend.warehouse.dto.request.AdjustmentRequest;
import com.stratiq.backend.warehouse.dto.response.DifferenceReportResponse;
import com.stratiq.backend.warehouse.service.StockService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StockService stockService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void getStockInfo_returnsOk() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(stockService.differenceReport(any(), any()))
                .thenReturn(new DifferenceReportResponse(itemId, "Widget", null, null, 100L, 30L, 70L));

        mockMvc.perform(get("/api/v1/stock").param("itemId", itemId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void getDifferenceReport_returnsOk() throws Exception {
        when(stockService.differenceReport(any(), any()))
                .thenReturn(new DifferenceReportResponse(null, null, null, null, 0L, 0L, 0L));

        mockMvc.perform(get("/api/v1/stock/difference-report"))
                .andExpect(status().isOk());
    }

    @Test
    void getStockInfo_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/stock"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "warehouse:read")
    void listMovements_returnsOk() throws Exception {
        when(stockService.findAll(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/stock/movements"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "warehouse:write")
    void postAdjustment_returnsCreated() throws Exception {
        AdjustmentRequest request = new AdjustmentRequest(UUID.randomUUID(), UUID.randomUUID(), 50L, "Restock");

        when(stockService.adjustment(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/stock/adjustment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
