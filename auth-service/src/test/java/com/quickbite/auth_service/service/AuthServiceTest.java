package com.quickbite.auth_service.service;

import com.quickbite.auth_service.dto.AuthResponse;
import com.quickbite.auth_service.dto.RegisterRequest;
import com.quickbite.auth_service.entity.RefreshToken;
import com.quickbite.auth_service.entity.User;
import com.quickbite.auth_service.entity.UserProfile;
import com.quickbite.auth_service.exception.AuthException;
import com.quickbite.auth_service.repository.RefreshTokenRepository;
import com.quickbite.auth_service.repository.UserProfileRepository;
import com.quickbite.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private User inactiveUser;
    private User suspendedUser;
    private final String validEmail = "user@test.com";
    private final String validPassword = "senha123";
    private final String accessToken = "access-token-123";
    private final String refreshToken = "refresh-token-456";

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
            .id(1L)
            .email(validEmail)
            .passwordHash("encoded-password")
            .fullName("Test User")
            .role(User.UserRole.CUSTOMER)
            .status(User.UserStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        inactiveUser = User.builder()
            .id(2L)
            .email("invactive@test.com")
            .passwordHash("encoded-password")
            .fullName("Test User")
            .role(User.UserRole.CUSTOMER)
            .status(User.UserStatus.INACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        suspendedUser = User.builder()
            .id(3L)
            .email("suspended@test.com")
            .passwordHash("encoded-password")
            .fullName("Suspended User")
            .role(User.UserRole.CUSTOMER)
            .status(User.UserStatus.SUSPENDED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void register_ShouldCreateUserSuccessfully() {

        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("new@test.com")
            .password("password123")
            .fullName("New User")
            .phone("11999999999")
            .address("Test Street, 123")
            .build();

        User newUser = User.builder()
            .id(4L)
            .email("new@test.com")
            .passwordHash("encoded-password")
            .fullName("New User")
            .role(User.UserRole.CUSTOMER)
            .status(User.UserStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(jwtService.getTokenExpirationInSeconds()).thenReturn(3600L);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(newUser, response.getUser());

        verify(userRepository).findByEmail("new@test.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(userProfileRepository).save((any(UserProfile.class)));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void register_ShouldThrowExceptionWhenEmailExists() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email(validEmail)
            .password("password123")
            .fullName("Existing User")
            .build();

        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(activeUser));

        AuthException exception = assertThrows(AuthException.class,
            () -> authService.register(registerRequest));

        assertEquals("Email já cadastrado", exception.getMessage());
        verify(userRepository).findByEmail(validEmail);
        verify(userRepository, never()).save(any(User.class));
        verify(userProfileRepository, never()).save(any(UserProfile.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void login_ShouldReturnAuthResponseWhenCredentialAreValid() {
        Authentication authentication  = mock(Authentication.class);

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(validEmail, validPassword)
        )).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(activeUser);
        when(jwtService.generateToken(activeUser)).thenReturn(accessToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            return invocation.<RefreshToken>getArgument(0);
        });

        AuthResponse response = authService.login(validEmail, validPassword);

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(activeUser, response.getUser());

        verify(authenticationManager).authenticate(
            new UsernamePasswordAuthenticationToken(validEmail, validPassword)
        );

        verify(jwtService).generateToken(activeUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_ShouldThrowAuthExceptionWhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        AuthException exception = assertThrows(AuthException.class,
            () -> authService.login(validEmail, "wrong-password"));

        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_ShouldThrowAuthExceptionWhenUserIsInactive() {
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(inactiveUser);

        AuthException exception = assertThrows(AuthException.class,
            () -> authService.login("inactive@test.com", validPassword));

        assertEquals("Usuário inativo", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_ShouldThrowAuthExceptionWhenUserIsSuspended() {
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(suspendedUser);

        AuthException exception = assertThrows(AuthException.class,
            () -> authService.login("suspended@test.com", validPassword));

        assertEquals("Usuário inativo", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_ShouldGenerateValidRefreshToken() {

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(activeUser);
        when(jwtService.generateToken(activeUser)).thenReturn(accessToken);

        AuthResponse response = authService.login(validEmail, validPassword);

        assertNotNull(response.getRefreshToken());

        verify(refreshTokenRepository).save(argThat(token ->
            token.getUser().equals(activeUser) &&
            !token.isRevoked() &&
            token.getExpiresAt().isAfter(LocalDateTime.now()) &&
            token.getToken() != null
        ));
    }

    @Test
    void login_ShouldHandleAuthenticationManagerExceptions() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new RuntimeException("Erro interno no servidor"));

        assertThrows(RuntimeException.class,
            () -> authService.login(validEmail, validPassword));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_ShouldThrowExceptionWhenAuthenticationFails() {

        String email = "test@example.com";
        String password = "password123";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credential"));

        AuthException exception = assertThrows(AuthException.class,
            () -> authService.login(email, password));

        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
