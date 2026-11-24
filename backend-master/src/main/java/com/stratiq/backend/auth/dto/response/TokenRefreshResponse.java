package com.stratiq.backend.auth.dto.response;

public record TokenRefreshResponse(String accessToken, String tokenType, long expiresIn) {}
