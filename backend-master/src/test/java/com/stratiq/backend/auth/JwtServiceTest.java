package com.stratiq.backend.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    // 64-byte secret satisfies HS512 minimum key length requirement.
    private static final String SECRET =
            "testSecretKeyForJwtUnitTestingMustBe64BytesLongForHS512Algorithm!!";
    private static final long EXPIRY_MS       = 86_400_000L; // 24 h
    private static final long REFRESH_EXPIRY  = 604_800_000L; // 7 d

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRY_MS);
        ReflectionTestUtils.setField(jwtService, "refreshExpirationMs", REFRESH_EXPIRY);
    }

    private UserDetails user(String email) {
        return User.withUsername(email).password("pw").authorities(List.of()).build();
    }

    @Test
    void generateToken_extractUsername_returnsSubject() {
        String token = jwtService.generateToken(user("alice@test.com"));

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@test.com");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        UserDetails u = user("alice@test.com");
        String token = jwtService.generateToken(u);

        assertThat(jwtService.isTokenValid(token, u)).isTrue();
    }

    @Test
    void isTokenValid_wrongUser_returnsFalse() {
        String token = jwtService.generateToken(user("alice@test.com"));

        assertThat(jwtService.isTokenValid(token, user("bob@test.com"))).isFalse();
    }

    @Test
    void generateToken_isRefreshToken_returnsFalse() {
        String token = jwtService.generateToken(user("alice@test.com"));

        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    @Test
    void generateRefreshToken_isRefreshToken_returnsTrue() {
        String token = jwtService.generateRefreshToken(user("alice@test.com"));

        assertThat(jwtService.isRefreshToken(token)).isTrue();
    }

    @Test
    void generateRefreshToken_extractUsername_returnsSubject() {
        String token = jwtService.generateRefreshToken(user("alice@test.com"));

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@test.com");
    }

    @Test
    void extractUsername_invalidToken_throwsException() {
        assertThatThrownBy(() -> jwtService.extractUsername("not.a.token"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getExpirationMs_returnsConfiguredValue() {
        assertThat(jwtService.getExpirationMs()).isEqualTo(EXPIRY_MS);
    }
}
