package com.quickbite.auth_service.service;

import com.quickbite.auth_service.dto.AuthResponse;
import com.quickbite.auth_service.dto.RegisterRequest;
import com.quickbite.auth_service.entity.RefreshToken;
import com.quickbite.auth_service.entity.User;
import com.quickbite.auth_service.entity.UserProfile;
import com.quickbite.auth_service.exception.AuthException;
import com.quickbite.auth_service.exception.InvalidUserStatusException;
import com.quickbite.auth_service.exception.UserAlreadyExistsException;
import com.quickbite.auth_service.exception.ValidationException;
import com.quickbite.auth_service.repository.RefreshTokenRepository;
import com.quickbite.auth_service.repository.UserProfileRepository;
import com.quickbite.auth_service.repository.UserRepository;
import com.quickbite.auth_service.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            return invocation.<RefreshToken>getArgument(0);
        });

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

        assertEquals(TestConstants.EMAIL_ALREADY_EXISTS_MESSAGE, exception.getMessage());
        verify(userRepository).findByEmail(validEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowValidationExceptionWhenEmailIsInvalid() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("invalid-email")
            .password("password123")
            .fullName("Test User")
            .build();

        ValidationException exception = assertThrows(ValidationException.class,
            () -> authService.register(registerRequest));

        assertEquals(TestConstants.INVALID_EMAIL_FORMAT_MESSAGE, exception.getMessage());;
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void register_ShouldThrowValidationExceptionWhenEmailIsEmpty() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("")
            .password("password123")
            .fullName("Test user")
            .build();

        ValidationException exception = assertThrows(ValidationException.class,
            () -> authService.register(registerRequest));

        assertEquals(TestConstants.EMAIL_REQUIRED_MESSAGE, exception.getMessage());
    }

    @Test
    void register_ShouldThrowValidationExceptionWhenPasswordIsTooShort() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email(TestConstants.VALID_EMAIL)
            .password(TestConstants.SHORT_PASSWORD)
            .fullName(TestConstants.VALID_FULL_NAME)
            .build();

        ValidationException exception = assertThrows(ValidationException.class,
            () -> authService.register(registerRequest));

        assertEquals(TestConstants.PASSWORD_TOO_SHORT_MESSAGE, exception.getMessage());
    }

    @Test
    void register_ShouldThrowUserAlreadyExistsException() {
        RegisterRequest request = RegisterRequest.builder()
            .email(TestConstants.VALID_EMAIL)
            .password(TestConstants.VALID_PASSWORD)
            .fullName(TestConstants.VALID_FULL_NAME)
            .build();

        when(userRepository.findByEmail(TestConstants.VALID_EMAIL))
            .thenReturn(Optional.of(mock(User.class)));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
            () -> authService.register(request));

        assertEquals(TestConstants.EMAIL_ALREADY_EXISTS_MESSAGE, exception.getMessage());
    }

    @Test
    void register_ShouldThrowValidationExceptionWhenPasswordIsEmpty() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@example.com")
            .password("")
            .fullName("Test User")
            .build();

        ValidationException exception = assertThrows(ValidationException.class,
            () -> authService.register(registerRequest));

        assertEquals(TestConstants.PASSWORD_REQUIRED_MESSAGE, exception.getMessage());
    }

    @Test
    void register_ShouldThrowValidationExceptionWhenFullNameIsEmpty() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@example.com")
            .password("password123")
            .fullName("")
            .build();

        ValidationException exception = assertThrows(ValidationException.class,
            () -> authService.register(registerRequest));

        assertEquals(TestConstants.FULL_NAME_REQUIRED_MESSAGE, exception.getMessage());
    }

    @Test
    void register_ShouldThrowValidationExceptionWhenFullNameIsTooShort() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@example.com")
            .password("password123")
            .fullName("A")
            .build();

        ValidationException exception = assertThrows(ValidationException.class,
            () -> authService.register(registerRequest));

        assertEquals(TestConstants.FULL_NAME_TOO_SHORT_MESSAGE , exception.getMessage());
    }

    @Test
    void register_ShouldThrowValidationExceptionWhenPhoneIsInvalid() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@example.com")
            .password("password123")
            .fullName("Test User")
            .phone("123")
            .build();

        ValidationException exception = assertThrows(ValidationException.class,
            () -> authService.register(registerRequest));

        assertEquals(TestConstants.INVALID_PHONE_MESSAGE, exception.getMessage());
    }

    @Test
    void register_ShouldAcceptValidPhoneNumber() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@example.com")
            .password("password123")
            .fullName("Test User")
            .phone("(11) 99999-9999")
            .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(jwtService.getTokenExpirationInSeconds()).thenReturn(3600L);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_ShouldReturnAuthResponseWhenCredentialsAreValid() {
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

        assertEquals(TestConstants.INVALID_CREDENTIALS_MESSAGE, exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ShouldThrowInvalidUserStatusExceptionWhenUserIsInactive() {

        String email = TestConstants.VALID_EMAIL;
        String password = TestConstants.VALID_PASSWORD;

        User invativeUser = User.builder()
            .email(email)
            .status(User.UserStatus.INACTIVE)
            .build();

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(inactiveUser);

        InvalidUserStatusException exception = assertThrows(InvalidUserStatusException.class,
            () -> authService.login(email, password));

        assertEquals(TestConstants.USER_INACTIVE_MESSAGE, exception.getMessage());
    }

    @Test
    void login_ShouldThrowAuthExceptionWhenAuthenticationFails() {

        String email = TestConstants.VALID_EMAIL;
        String password = TestConstants.VALID_PASSWORD;

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        AuthException exception = assertThrows(AuthException.class,
            () -> authService.login(email, password));

        assertEquals(TestConstants.INVALID_CREDENTIALS_MESSAGE, exception.getMessage());
    }

    @Test
    void login_ShouldThrowInvalidUserStatusExceptionWhenUserIsSuspended() {
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(suspendedUser);

        InvalidUserStatusException exception = assertThrows(InvalidUserStatusException.class,
            () -> authService.login("suspended@test.com", validPassword));

        assertEquals(TestConstants.USER_INACTIVE_MESSAGE, exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ShouldThrowValidationExceptionWhenEmailIsInvalid() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> authService.login("invalid-email", validPassword));

        assertEquals(TestConstants.INVALID_EMAIL_FORMAT_MESSAGE, exception.getMessage());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_ShouldThrowValidationExceptionWhenPasswordIsEmpty() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> authService.login(validEmail, ""));

        assertEquals(TestConstants.PASSWORD_REQUIRED_MESSAGE, exception.getMessage());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_ShouldHandleDisabledUserException() {

        String email = "user@example.com";
        String password = "password123";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new DisabledException("Usuário desabilitado"));

        InvalidUserStatusException exception = assertThrows(InvalidUserStatusException.class,
            () -> authService.login(email, password));

        assertEquals(TestConstants.USER_DISABLED_MESSAGE, exception.getMessage());

        verify(authenticationManager).authenticate(argThat(token ->
            token.getPrincipal().equals(email) &&
            token.getCredentials().equals(password)
        ));
    }

    @Test
    void login_ShouldThrowAuthExceptionWhenUserIsInactive() {
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(inactiveUser);

        AuthException exception = assertThrows(AuthException.class,
            () -> authService.login("inactive@test.com", validPassword));

        assertEquals(TestConstants.USER_INACTIVE_MESSAGE, exception.getMessage());
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

        assertEquals(TestConstants.USER_INACTIVE_MESSAGE, exception.getMessage());
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
    void refreshToken_ShouldReturnNewTokensWhenValid() {

        User activeUser = User.builder()
            .id(TestConstants.USER_ID)
            .email(TestConstants.VALID_EMAIL)
            .status(User.UserStatus.ACTIVE)
            .build();

        RefreshToken validRefreshToken = RefreshToken.builder()
            .id(1L)
            .token(TestConstants.VALID_REFRESH_TOKEN)
            .user(activeUser)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .revoked(false)
            .build();

        when(refreshTokenRepository.findByToken(TestConstants.VALID_REFRESH_TOKEN)).thenReturn(Optional.of(validRefreshToken));
        when(jwtService.generateToken(activeUser)).thenReturn(TestConstants.NEW_ACCESS_TOKEN);

        AuthResponse response = authService.refreshToken(TestConstants.VALID_REFRESH_TOKEN);

        assertNotNull(response);
        assertEquals(TestConstants.NEW_ACCESS_TOKEN, response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(TestConstants.TOKEN_EXPIRATION, response.getExpiresIn());
    }

    @Test
    void refreshToken_ShouldThrowExceptionWhenTokenIsEmpty() {
        String emptyToken = "";

        AuthException exception = assertThrows(AuthException.class,
            () -> authService.refreshToken(emptyToken));

        assertEquals(TestConstants.REFRESH_TOKEN_REQUIRED_MESSAGE, exception.getMessage());
    }

    @Test
    void refreshToken_ShouldThrowExceptionWhenTokenIsInvalid() {
        when(refreshTokenRepository.findByToken("invalid-token"))
            .thenReturn(Optional.empty());

        AuthException exception = assertThrows(AuthException.class,
            () -> authService.refreshToken("invalid-token"));

        assertEquals(TestConstants.INVALID_REFRESH_TOKEN_MESSAGE, exception.getMessage());
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

        assertEquals(TestConstants.INVALID_CREDENTIALS_MESSAGE, exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
