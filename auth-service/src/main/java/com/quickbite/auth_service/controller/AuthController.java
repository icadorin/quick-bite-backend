package com.quickbite.auth_service.controller;

import com.quickbite.auth_service.constants.ApiPaths;
import com.quickbite.auth_service.dto.LoginResponse;
import com.quickbite.auth_service.dto.RefreshTokenRequest;
import com.quickbite.auth_service.dto.RegisterRequest;
import com.quickbite.auth_service.dto.LoginRequest;
import com.quickbite.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.AUTH)
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping(ApiPaths.REGISTER)
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse register(
        @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

   @PostMapping(ApiPaths.LOGIN)
    public LoginResponse login(
        @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping(ApiPaths.REFRESH_TOKEN)
    public LoginResponse refreshToken(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        return authService.refreshToken(request.getRefreshToken());
    }
}
