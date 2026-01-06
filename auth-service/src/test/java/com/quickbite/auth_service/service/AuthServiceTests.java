package com.quickbite.auth_service.service;

import com.quickbite.auth_service.constants.TestConstants;
import com.quickbite.auth_service.dto.LoginRequest;
import com.quickbite.auth_service.dto.LoginResponse;
import com.quickbite.auth_service.dto.RegisterRequest;
import com.quickbite.auth_service.entity.RefreshToken;
import com.quickbite.auth_service.entity.User;
import com.quickbite.auth_service.entity.UserProfile;
import com.quickbite.auth_service.exception.AuthException;
import com.quickbite.auth_service.exception.InvalidUserStatusException;
import com.quickbite.auth_service.exception.UserAlreadyExistsException;
import com.quickbite.auth_service.mapper.UserCreateMapper;
import com.quickbite.auth_service.mapper.UserResponseMapper;
import com.quickbite.auth_service.repository.RefreshTokenRepository;
import com.quickbite.auth_service.repository.UserProfileRepository;
import com.quickbite.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

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

    @Mock
    private UserCreateMapper createMapper;

    @Mock
    private UserResponseMapper responseMapper;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User activeUser;
    private RefreshToken validRefreshToken;

    @BeforeEach
    void setUp() {
        validRegisterRequest = RegisterRequest.builder()
            .fullName(TestConstants.VALID_FULL_NAME)
            .email(TestConstants.VALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .build();

        validLoginRequest = LoginRequest.builder()
            .email(TestConstants.VALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .build();

        activeUser = User.builder()
            .id(TestConstants.VALID_USER_ID)
            .email(TestConstants.VALID_EMAIL)
            .fullName(TestConstants.VALID_FULL_NAME)
            .status(User.UserStatus.ACTIVE)
            .role(User.UserRole.CUSTOMER)
            .build();

        validRefreshToken = RefreshToken.builder()
            .token(TestConstants.VALID_REFRESH_TOKEN)
            .user(activeUser)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .revoked(false)
            .build();
    }

    @Test
    void register_shouldCreateUserAndReturnLoginResponse_whenRequestIsValid() {
        when(userRepository.existsByEmail(TestConstants.VALID_EMAIL))
            .thenReturn(false);
        when(createMapper.toEntity(validRegisterRequest))
            .thenReturn(activeUser);
        when(passwordEncoder.encode(TestConstants.VALID_PASSWORD))
            .thenReturn("hashed");
        when(userRepository.save(any(User.class)))
            .thenReturn(activeUser);
        when(jwtService.generateToken(activeUser))
            .thenReturn(TestConstants.VALID_ACCESS_TOKEN);
        when(jwtService.getTokenExpirationInSeconds())
            .thenReturn(TestConstants.TOKEN_EXPIRATION_SECONDS);
        when(responseMapper.toResponse(activeUser))
            .thenReturn(TestConstants.USER_RESPONSE);

        LoginResponse response = authService.register(validRegisterRequest);

        assertNotNull(response);
        verify(userRepository).save(activeUser);
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(TestConstants.VALID_EMAIL))
            .thenReturn(true);

        assertThrows(
            UserAlreadyExistsException.class,
            () -> authService.register(validRegisterRequest)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnLoginResponse_whenCredentialsAreValid() {
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal())
            .thenReturn(activeUser);
        when(jwtService.generateToken(activeUser))
            .thenReturn(TestConstants.VALID_ACCESS_TOKEN);
        when(jwtService.getTokenExpirationInSeconds())
            .thenReturn(TestConstants.TOKEN_EXPIRATION_SECONDS);
        when(responseMapper.toResponse(activeUser))
            .thenReturn(TestConstants.USER_RESPONSE);

        LoginResponse response = authService.login(validLoginRequest);

        assertNotNull(response);
        verify(authenticationManager).authenticate(any());

    }

    @Test
    void login_shouldThrow_whenUserIsInactive() {
        activeUser.setStatus(User.UserStatus.INACTIVE);

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any()))
            .thenReturn(authentication);
        when(authentication.getPrincipal())
            .thenReturn(activeUser);

        assertThrows(
            InvalidUserStatusException.class,
            () -> authService.login(validLoginRequest)
        );
    }

    @Test
    void refreshToken_shouldReturnNewTokens_whenTokenIsValid() {
        when(refreshTokenRepository.findByToken(TestConstants.VALID_REFRESH_TOKEN))
            .thenReturn(Optional.of(validRefreshToken));
        when(jwtService.generateToken(activeUser))
            .thenReturn(TestConstants.VALID_ACCESS_TOKEN);
        when(jwtService.getTokenExpirationInSeconds())
            .thenReturn(TestConstants.TOKEN_EXPIRATION_SECONDS);
        when(responseMapper.toResponse(activeUser))
            .thenReturn(TestConstants.USER_RESPONSE);

        LoginResponse response =
            authService.refreshToken(TestConstants.VALID_REFRESH_TOKEN);

        assertNotNull(response);

        verify(refreshTokenRepository, times(2))
            .save(any(RefreshToken.class));
    }

    @Test
    void refreshToken_shouldThrow_whenTokenDoesNotExist() {
        when(refreshTokenRepository.findByToken(TestConstants.VALID_REFRESH_TOKEN))
            .thenReturn(Optional.empty());

        assertThrows(
            AuthException.class,
            () -> authService.refreshToken(TestConstants.VALID_REFRESH_TOKEN)
        );
    }

    @Test
    void refreshToken_shouldThrow_whenTokenIsRevoked() {
        validRefreshToken.setRevoked(true);

        when(refreshTokenRepository.findByToken(TestConstants.VALID_REFRESH_TOKEN))
            .thenReturn(Optional.of(validRefreshToken));

        assertThrows(
            AuthException.class,
            () -> authService.refreshToken(TestConstants.VALID_REFRESH_TOKEN)
        );

        verify(refreshTokenRepository,  never()).save(any());
    }
}
