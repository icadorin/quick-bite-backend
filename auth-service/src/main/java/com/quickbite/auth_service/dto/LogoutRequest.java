package com.quickbite.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank
    String refreshToken
) {}
