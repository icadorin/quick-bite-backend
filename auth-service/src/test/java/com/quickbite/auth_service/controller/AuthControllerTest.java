package com.quickbite.auth_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.auth_service.constants.TestConstants;
import com.quickbite.auth_service.dto.LoginRequest;
import com.quickbite.auth_service.dto.LoginResponse;
import com.quickbite.auth_service.dto.RefreshTokenRequest;
import com.quickbite.auth_service.dto.RegisterRequest;
import com.quickbite.auth_service.exception.GlobalExceptionHandler;
import com.quickbite.auth_service.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
            .fullName(TestConstants.VALID_FULL_NAME)
            .email(TestConstants.INVALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .build();

        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.email")
                .value(TestConstants.EMAIL_INVALID_MESSAGE));
    }

    @Test
    void register_shouldReturn201_whenRequestIsValid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
            .fullName(TestConstants.VALID_FULL_NAME)
            .email(TestConstants.VALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .build();

        when(authService.register(any()))
            .thenReturn(new LoginResponse());

        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        verify(authService).register(any());
    }

    @Test
    void login_shouldReturn400_whenPasswordIsBlank() throws Exception {
        LoginRequest request = LoginRequest.builder()
            .email(TestConstants.VALID_EMAIL)
            .password("")
            .build();

        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.password")
                .value(TestConstants.PASSWORD_REQUIRED_MESSAGE));
    }

    @Test
    void login_shouldReturn200_whenRequestIdValid() throws Exception {
        LoginRequest request = LoginRequest.builder()
            .email(TestConstants.VALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .build();

        when(authService.login(any()))
            .thenReturn(new LoginResponse());

        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(authService).login(any());
    }

    @Test
    void refreshToken_shouldReturn400_whenTokenIsBlank() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
            .refreshToken("")
            .build();

        mockMvc.perform(post("/api/auth/refresh-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.refreshToken")
                .value(TestConstants.REFRESH_TOKEN_REQUIRED_MESSAGE));
    }

    @Test
    void refreshToken_shouldReturn200_whenRequestIdValid() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
            .refreshToken(TestConstants.VALID_REFRESH_TOKEN)
            .build();

        when(authService.refreshToken(TestConstants.VALID_REFRESH_TOKEN))
            .thenReturn(new LoginResponse());

        mockMvc.perform(post("/api/auth/refresh-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(authService)
            .refreshToken(TestConstants.VALID_REFRESH_TOKEN);
    }
}
