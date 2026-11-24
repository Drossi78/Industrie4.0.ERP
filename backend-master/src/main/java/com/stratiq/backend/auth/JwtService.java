package com.stratiq.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    // JWT token generation and validation (HS512). Access token 24h, refresh token 7d.

    @Value("${app.jwt.secret}")
    private String secret;

    @Getter
    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // Derives signing key from secret. Must be 256 bits minimum for HS512.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Access token: short-lived, contains username and roles.
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, expirationMs);
    }

    // Refresh token: longer-lived, used to get new access token when expired.
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(Map.of("refresh", true), userDetails, refreshExpirationMs);
    }

    private String generateToken(Map<String, Object> claims, UserDetails userDetails, long expiry) {
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload()
        );
    }

    // Returns true only if the token was issued as a refresh token.
    // Prevents attacker from long term access with a stolen token
    public boolean isRefreshToken(String token) {
        return Boolean.TRUE.equals(extractClaim(token, claims -> claims.get("refresh", Boolean.class)));
    }
}
