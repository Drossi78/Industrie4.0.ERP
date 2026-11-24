package com.stratiq.backend.auth;

import com.stratiq.backend.auth.dto.request.LoginRequest;
import com.stratiq.backend.auth.dto.response.LoginResponse;
import com.stratiq.backend.auth.dto.request.TokenRefreshRequest;
import com.stratiq.backend.auth.dto.response.TokenRefreshResponse;
import com.stratiq.backend.user.dto.request.ChangePasswordRequest;
import com.stratiq.backend.user.model.AppUser;
import com.stratiq.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // @Valid validates username/password not blank. Backend returns 400 if validation fails (expected).
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password())
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).build();
        }
        var userDetails = userDetailsService.loadUserByUsername(request.username());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        List<String> permissions = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        return ResponseEntity.ok(new LoginResponse(
            accessToken, refreshToken, "Bearer", jwtService.getExpirationMs(), request.username(), permissions));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal AppUser currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        try {
            String username = jwtService.extractUsername(request.refreshToken());
            var userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(request.refreshToken(), userDetails)
                    && jwtService.isRefreshToken(request.refreshToken())) {
                String newToken = jwtService.generateToken(userDetails);
                return ResponseEntity.ok(new TokenRefreshResponse(newToken, "Bearer", jwtService.getExpirationMs()));
            }
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}
