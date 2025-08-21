package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.UserResponse;
import dev.pollywag.nimbusdrop.entity.TokenType;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.entity.VerificationToken;
import dev.pollywag.nimbusdrop.exception.ResourceOwnershipException;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import dev.pollywag.nimbusdrop.repository.VerificationTokenRepository;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    public UserService(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder,
                       VerificationTokenRepository verificationTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
    }

    public UserResponse changeUsername(Long userId, String newUsername, String email) {
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if(!user.getUserDisplayName().equals(userDisplayName)) {
            throw new ResourceOwnershipException("You are not allowed to change the username of the user");
        }

        user.setUsername(newUsername);
        user= userRepository.save(user);

        return modelMapper.map(user, UserResponse.class);
    }

    public String changePassword(Long userId, String newPassword, String oldPassword, String email) {
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if(!user.getUserDisplayName().equals(userDisplayName)) {
            throw new ResourceOwnershipException("You are not allowed to change the password of the user");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password does not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password changed successfully";
    }

    public String changeEmail(Long userId, String newEmail, String password, String email) {
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if(!user.getUserDisplayName().equals(userDisplayName)) {
            throw new ResourceOwnershipException("You are not allowed to change the email of the user");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Password does not match");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(expiry);
        verificationToken.setNewEmail(newEmail);
        verificationToken.setType(TokenType.EMAIL_CHANGE);

        user.getVerificationTokens().add(verificationToken);

        userRepository.save(user);

        emailService.sendConfirmationNewEmail(newEmail, token);

        return "Please check your email to confirm your email.";

    }

    public String deleteToken(Long userId, String email) {
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if(!user.getUserDisplayName().equals(userDisplayName)) {
            throw new ResourceOwnershipException("You are not allowed to get code from this user");
        }
        String token = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(expiry);
        verificationToken.setType(TokenType.DELETE_ACCOUNT);
        user.getVerificationTokens().add(verificationToken);
        userRepository.save(user);
        emailService.sendTokenCodeForDeletion(email, token);

        return "Check your email for the code";
    }
}
