package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.entity.*;
import dev.pollywag.nimbusdrop.exception.InvalidPasswordException;
import dev.pollywag.nimbusdrop.repository.NimbusRepository;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // --- Mocked Dependencies ---
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private NimbusRepository nimbusRepository;
    @Mock private EntityFetcher entityFetcher;

    // --- Class Under Test ---
    @InjectMocks
    private UserService userService;

    // --- Reusable Test Data ---
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("testuser");
        mockUser.setPassword("encoded_old_password");
        mockUser.setVerificationTokens(new ArrayList<>());
    }

    // =====================================================================
    // getUser()
    // =====================================================================

    @Test
    void getUser_shouldReturnUser_whenUserExists() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);

        // Act
        User result = userService.getUser("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getUserDisplayName());
    }

    @Test
    void getUser_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(entityFetcher.getUserByEmail("unknown@example.com"))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                userService.getUser("unknown@example.com"));
    }

    // =====================================================================
    // changeUsername()
    // =====================================================================

    @Test
    void changeUsername_shouldUpdateUsername_whenUserExists() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        // Act
        User result = userService.changeUsername("newUsername", "test@example.com");

        // Assert
        assertEquals("newUsername", result.getUserDisplayName());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void changeUsername_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(entityFetcher.getUserByEmail("unknown@example.com"))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                userService.changeUsername("newUsername", "unknown@example.com"));

        verify(userRepository, never()).save(any());
    }

    // =====================================================================
    // changePassword()
    // =====================================================================

    @Test
    void changePassword_shouldUpdatePassword_whenOldPasswordMatches() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(passwordEncoder.matches("old_password", "encoded_old_password")).thenReturn(true);
        when(passwordEncoder.encode("new_password")).thenReturn("encoded_new_password");
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        // Act
        userService.changePassword("new_password", "old_password", "test@example.com");

        // Assert
        assertEquals("encoded_new_password", mockUser.getPassword());
        verify(userRepository, times(1)).save(mockUser);
        verify(passwordEncoder, times(1)).encode("new_password");
    }

    @Test
    void changePassword_shouldThrowIllegalArgumentException_whenOldPasswordDoesNotMatch() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(passwordEncoder.matches("wrong_password", "encoded_old_password")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                userService.changePassword("new_password", "wrong_password", "test@example.com"));

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    // =====================================================================
    // changeEmail()
    // =====================================================================

    @Test
    void changeEmail_shouldSendVerificationEmail_whenPasswordMatches() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(passwordEncoder.matches("correct_password", "encoded_old_password")).thenReturn(true);
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        doNothing().when(emailService).sendConfirmationNewEmail(anyString(), anyString());

        // Act
        userService.changeEmail("newemail@example.com", "correct_password", "test@example.com");

        // Assert
        verify(emailService, times(1)).sendConfirmationNewEmail(eq("newemail@example.com"), anyString());
        verify(userRepository, times(1)).save(mockUser);

        // Verify verification token was added with correct type
        boolean hasEmailChangeToken = mockUser.getVerificationTokens().stream()
                .anyMatch(t -> t.getType() == TokenType.EMAIL_CHANGE);
        assertTrue(hasEmailChangeToken);
    }

    @Test
    void changeEmail_shouldThrowInvalidPasswordException_whenPasswordDoesNotMatch() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(passwordEncoder.matches("wrong_password", "encoded_old_password")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () ->
                userService.changeEmail("newemail@example.com", "wrong_password", "test@example.com"));

        verify(emailService, never()).sendConfirmationNewEmail(anyString(), anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changeEmail_shouldAddVerificationTokenToUser_whenPasswordMatches() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(passwordEncoder.matches("correct_password", "encoded_old_password")).thenReturn(true);
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        doNothing().when(emailService).sendConfirmationNewEmail(anyString(), anyString());

        // Act
        userService.changeEmail("newemail@example.com", "correct_password", "test@example.com");

        // Assert — token was added with correct new email
        VerificationToken addedToken = mockUser.getVerificationTokens().get(0);
        assertEquals("newemail@example.com", addedToken.getNewEmail());
        assertEquals(TokenType.EMAIL_CHANGE, addedToken.getType());
        assertNotNull(addedToken.getToken());
    }

    // =====================================================================
    // deleteToken()
    // =====================================================================

    @Test
    void deleteToken_shouldSendDeletionCode_whenUserExists() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        doNothing().when(emailService).sendTokenCodeForDeletion(anyString(), anyString());

        // Act
        userService.deleteToken("test@example.com");

        // Assert
        verify(emailService, times(1)).sendTokenCodeForDeletion(eq("test@example.com"), anyString());
        verify(userRepository, times(1)).save(mockUser);

        // Verify deletion token was added
        boolean hasDeleteToken = mockUser.getVerificationTokens().stream()
                .anyMatch(t -> t.getType() == TokenType.DELETE_ACCOUNT);
        assertTrue(hasDeleteToken);
    }

    @Test
    void deleteToken_shouldAddSixDigitToken_whenUserExists() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        doNothing().when(emailService).sendTokenCodeForDeletion(anyString(), anyString());

        // Act
        userService.deleteToken("test@example.com");

        // Assert — token should be exactly 6 digits
        VerificationToken addedToken = mockUser.getVerificationTokens().get(0);
        assertNotNull(addedToken.getToken());
        assertEquals(6, addedToken.getToken().length());
        assertTrue(addedToken.getToken().matches("\\d{6}"));
    }

    // =====================================================================
    // findAllNimbusByUserId()
    // =====================================================================

    @Test
    void findAllNimbusByUserId_shouldReturnNimbusList_whenUserExists() {
        // Arrange
        List<Nimbus> mockNimbusList = List.of(new Nimbus(), new Nimbus(), new Nimbus());

        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(nimbusRepository.findByUserId(1L)).thenReturn(mockNimbusList);

        // Act
        List<Nimbus> result = userService.findAllNimbusByUserId("test@example.com");

        // Assert
        assertEquals(3, result.size());
        verify(nimbusRepository, times(1)).findByUserId(1L);
    }

    @Test
    void findAllNimbusByUserId_shouldReturnEmptyList_whenUserHasNoNimbus() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(nimbusRepository.findByUserId(1L)).thenReturn(List.of());

        // Act
        List<Nimbus> result = userService.findAllNimbusByUserId("test@example.com");

        // Assert
        assertTrue(result.isEmpty());
        verify(nimbusRepository, times(1)).findByUserId(1L);
    }
}