package com.quickbite.auth_service.controller;

import com.quickbite.auth_service.constants.ApiPaths;
import com.quickbite.auth_service.dto.*;
import com.quickbite.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.AUTH)
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping(ApiPaths.REGISTER)
    @PreAuthorize("permitAll()")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse register(
        @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

   @PostMapping(ApiPaths.LOGIN)
   @PreAuthorize("permitAll()")
    public LoginResponse login(
        @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping(ApiPaths.REFRESH_TOKEN)
    @PreAuthorize("permitAll()")
    public LoginResponse refreshToken(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        return authService.refreshToken(request.getRefreshToken());
    }

    @PostMapping(ApiPaths.LOGOUT)
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
        @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(request.refreshToken());
    }
}
