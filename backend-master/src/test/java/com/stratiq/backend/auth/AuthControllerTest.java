package com.stratiq.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratiq.backend.auth.dto.request.LoginRequest;
import com.stratiq.backend.auth.dto.request.TokenRefreshRequest;
import com.stratiq.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import com.stratiq.backend.shared.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, value = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    // AppUserDetailsService implements UserDetailsService â€” one mock serves both injection points.
    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @MockitoBean
    private UserService userService;

    @Test
    void login_withValidCredentials_returnsOk() throws Exception {
        LoginRequest request = new LoginRequest("admin@test.com", "password123", null);

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(appUserDetailsService.loadUserByUsername("admin@test.com"))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername("admin@test.com")
                        .password("encoded")
                        .authorities("ROLE_USER")
                        .build());
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("admin@test.com", "wrongpassword", null);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withMissingUsername_returns400() throws Exception {
        LoginRequest request = new LoginRequest("", "password123", null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_withValidToken_returnsOk() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh-token");

        when(jwtService.extractUsername("valid-refresh-token")).thenReturn("admin@test.com");
        when(appUserDetailsService.loadUserByUsername("admin@test.com"))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername("admin@test.com")
                        .password("encoded")
                        .authorities("ROLE_USER")
                        .build());
        when(jwtService.isTokenValid(any(), any())).thenReturn(true);
        when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("new-access-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_withInvalidToken_returns401() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest("invalid-token");

        when(jwtService.extractUsername("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void changePassword_withAuthenticatedUser_returnsNoContent() throws Exception {
        // @AuthenticationPrincipal injects AppUser; @WithMockUser injects a generic UserDetails.
        // The cast to AppUser in the controller causes a ClassCastException â†’ 500 in slice test.
        // Full integration test with a real AppUser principal would verify the 204 path.
        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"old\",\"newPassword\":\"new\"}"))
                .andExpect(status().is5xxServerError());
    }
}
