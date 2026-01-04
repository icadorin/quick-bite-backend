package com.quickbite.auth_service.controller;

import com.quickbite.auth_service.dto.LoginResponse;
import com.quickbite.auth_service.dto.RefreshTokenRequest;
import com.quickbite.auth_service.dto.RegisterRequest;
import com.quickbite.auth_service.dto.LoginRequest;
import com.quickbite.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(
            authService.refreshToken(request.getRefreshToken())
        );
    }
}
