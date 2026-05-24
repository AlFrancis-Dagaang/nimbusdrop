package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.AuthResponseHolder;
import dev.pollywag.nimbusdrop.dto.respondeDTO.UserResponse;
import dev.pollywag.nimbusdrop.entity.*;
import dev.pollywag.nimbusdrop.exception.TokenRefreshException;
import dev.pollywag.nimbusdrop.exception.UserAlreadyExistsException;
import dev.pollywag.nimbusdrop.exception.UserNotEnableException;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import dev.pollywag.nimbusdrop.repository.VerificationTokenRepository;
import dev.pollywag.nimbusdrop.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // --- Mocked Dependencies ---
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private EmailService emailService;
    @Mock private EntityFetcher entityFetcher;
    @Mock private ModelMapper modelMapper;
    @Mock private VerificationTokenRepository verificationTokenRepository;

    // --- Class Under Test ---
    @InjectMocks
    private AuthService authService;

    // --- Reusable Test Data ---
    private User mockUser;
    private VerificationToken mockVerificationToken;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encoded_password");
        mockUser.setEnabled(true);
        mockUser.setVerificationTokens(new ArrayList<>());

        mockVerificationToken = new VerificationToken();
        mockVerificationToken.setToken("valid-token-uuid");
        mockVerificationToken.setUser(mockUser);
        mockVerificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        mockVerificationToken.setUsed(false);
        mockVerificationToken.setType(TokenType.SIGNUP_CONFIRM);
    }

    // signup()
    @Test
    void signup_shouldSaveUser_whenEmailIsNotTaken() {
        // Arrange
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());

        // Act
        authService.signup("newuser@example.com", "newuser", "password123", Role.USER);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendConfirmationEmail(anyString(), anyString());
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void signup_shouldThrowUserAlreadyExistsException_whenEmailAlreadyTaken() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () ->
                authService.signup("test@example.com", "testuser", "password123", Role.USER));

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString());
    }

    @Test
    void signup_shouldDefaultToUserRole_whenRoleIsNull() {
        // Arrange
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());

        // Act
        authService.signup("newuser@example.com", "newuser", "password123", null);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signup_shouldSaveUserWithDisabledAccount_untilEmailConfirmed() {
        // Arrange
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            assertFalse(saved.getEnabled(), "User should be disabled until email is confirmed");
            return saved;
        });
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());

        // Act
        authService.signup("newuser@example.com", "newuser", "password123", Role.USER);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    // authenticate()
    @Test
    void authenticate_shouldReturnTokens_whenCredentialsAreValid() {
        // Arrange
        UserResponse mockUserResponse = new UserResponse();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(jwtService.generateAccessToken(mockUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(mockUser)).thenReturn("refresh-token");
        when(modelMapper.map(mockUser, UserResponse.class)).thenReturn(mockUserResponse);
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(1));

        // Act
        AuthResponseHolder result = authService.authenticate("test@example.com", "password123");

        // Assert
        assertNotNull(result);
        assertEquals("refresh-token", result.getRefreshToken());
        verify(jwtService, times(1)).generateAccessToken(mockUser);
        verify(jwtService, times(1)).generateRefreshToken(mockUser);
    }

    @Test
    void authenticate_shouldThrowUserNotEnableException_whenUserIsNotEnabled() {
        // Arrange
        mockUser.setEnabled(false);

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);

        // Act & Assert
        assertThrows(UserNotEnableException.class, () ->
                authService.authenticate("test@example.com", "password123"));

        verify(jwtService, never()).generateAccessToken(any());
    }

    // =====================================================================
    // refreshToken()
    // =====================================================================

    @Test
    void refreshToken_shouldReturnNewTokens_whenRefreshTokenIsValid() {
        // Arrange
        UserResponse mockUserResponse = new UserResponse();

        when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-refresh-token")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(jwtService.isTokenValid("valid-refresh-token", mockUser)).thenReturn(true);
        when(jwtService.generateAccessToken(mockUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(mockUser)).thenReturn("new-refresh-token");
        when(modelMapper.map(mockUser, UserResponse.class)).thenReturn(mockUserResponse);
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(1));

        // Act
        AuthResponseHolder result = authService.refreshToken("valid-refresh-token");

        // Assert
        assertNotNull(result);
        assertEquals("new-refresh-token", result.getRefreshToken());
        verify(jwtService, times(1)).generateAccessToken(mockUser);
    }

    @Test
    void refreshToken_shouldThrowTokenRefreshException_whenTokenIsNotRefreshType() {
        // Arrange
        when(jwtService.isRefreshToken("bad-token")).thenReturn(false);

        // Act & Assert
        assertThrows(TokenRefreshException.class, () ->
                authService.refreshToken("bad-token"));

        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void refreshToken_shouldThrowTokenRefreshException_whenTokenIsExpiredOrInvalid() {
        // Arrange
        when(jwtService.isRefreshToken("expired-token")).thenReturn(true);
        when(jwtService.extractUsername("expired-token")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(jwtService.isTokenValid("expired-token", mockUser)).thenReturn(false);

        // Act & Assert
        assertThrows(TokenRefreshException.class, () ->
                authService.refreshToken("expired-token"));

        verify(jwtService, never()).generateAccessToken(any());
    }

    // signUpConfirmation()
    @Test
    void signUpConfirmation_shouldEnableUser_whenTokenIsValid() {
        // Arrange
        when(entityFetcher.getVerificationTokenByToken("valid-token-uuid"))
                .thenReturn(mockVerificationToken);
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        // Act
        authService.signUpConfirmation("valid-token-uuid");

        // Assert
        assertTrue(mockUser.getEnabled());
        assertTrue(mockVerificationToken.isUsed());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void signUpConfirmation_shouldThrowException_whenTokenIsAlreadyUsed() {
        // Arrange
        mockVerificationToken.setUsed(true);

        when(entityFetcher.getVerificationTokenByToken("used-token"))
                .thenReturn(mockVerificationToken);

        // Act & Assert
        assertThrows(Exception.class, () ->
                authService.signUpConfirmation("used-token"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void signUpConfirmation_shouldThrowException_whenTokenIsExpired() {
        // Arrange
        mockVerificationToken.setExpiryDate(LocalDateTime.now().minusMinutes(5));

        when(entityFetcher.getVerificationTokenByToken("expired-token"))
                .thenReturn(mockVerificationToken);

        // Act & Assert
        assertThrows(Exception.class, () ->
                authService.signUpConfirmation("expired-token"));

        verify(userRepository, never()).save(any());
    }

    // resendEmailVerificationToken()
    @Test
    void resendEmailVerificationToken_shouldUpdateExpiryAndResendEmail() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getVerificationTokenByUserIdAndType(1L, TokenType.SIGNUP_CONFIRM))
                .thenReturn(mockVerificationToken);
        when(verificationTokenRepository.save(mockVerificationToken)).thenReturn(mockVerificationToken);
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString());

        // Act
        authService.resendEmailVerificationToken("test@example.com");

        // Assert
        assertFalse(mockVerificationToken.isUsed());
        assertTrue(mockVerificationToken.getExpiryDate().isAfter(LocalDateTime.now()));
        verify(verificationTokenRepository, times(1)).save(mockVerificationToken);
        verify(emailService, times(1)).sendConfirmationEmail("test@example.com", mockVerificationToken.getToken());
    }

    // =====================================================================
    // setNewUserPassword()
    // =====================================================================

    @Test
    void setNewUserPassword_shouldUpdatePassword_whenTokenIsValid() {
        // Arrange
        mockVerificationToken.setType(TokenType.PASSWORD_FORGOT);

        when(entityFetcher.getVerificationTokenByToken("valid-reset-token"))
                .thenReturn(mockVerificationToken);
        when(passwordEncoder.encode("newPassword123")).thenReturn("new_encoded_password");
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        // Act
        authService.setNewUserPassword("valid-reset-token", "newPassword123");

        // Assert
        assertEquals("new_encoded_password", mockUser.getPassword());
        assertTrue(mockVerificationToken.isUsed());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void setNewUserPassword_shouldThrowException_whenTokenIsExpired() {
        // Arrange
        mockVerificationToken.setExpiryDate(LocalDateTime.now().minusMinutes(5));

        when(entityFetcher.getVerificationTokenByToken("expired-token"))
                .thenReturn(mockVerificationToken);

        // Act & Assert
        assertThrows(Exception.class, () ->
                authService.setNewUserPassword("expired-token", "newPassword123"));

        verify(userRepository, never()).save(any());
    }
}