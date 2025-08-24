package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.NimbusResponse;
import dev.pollywag.nimbusdrop.dto.respondeDTO.UserResponse;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.entity.TokenType;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.entity.VerificationToken;
import dev.pollywag.nimbusdrop.exception.InvalidPasswordException;
import dev.pollywag.nimbusdrop.exception.ResourceOwnershipException;
import dev.pollywag.nimbusdrop.repository.NimbusRepository;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import dev.pollywag.nimbusdrop.repository.VerificationTokenRepository;
import jakarta.persistence.EntityManager;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final NimbusRepository nimbusRepository;
    private final EntityFetcher entityFetcher;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       EmailService emailService, NimbusRepository nimbusRepository, EntityFetcher entityFetcher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.nimbusRepository = nimbusRepository;
        this.entityFetcher = entityFetcher;
    }

    public User changeUsername(String newUsername, String email) {
        // Fetch the authenticated user
        User user = entityFetcher.getUserByEmail(email);

        //Set the new Username
        user.setUsername(newUsername);


        return userRepository.save(user);
    }

    public void changePassword(String newPassword, String oldPassword, String email) {
        // Fetch the authenticated user
        User user = entityFetcher.getUserByEmail(email);

        // Validate the old password
        String currentPassword = user.getPassword();
        if (!passwordEncoder.matches(oldPassword, currentPassword)) {
            throw new IllegalArgumentException("Old password does not match");
        }

        // Encode and set the new password
        String updatedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(updatedPassword);


        userRepository.save(user);
    }


    public void changeEmail(String newEmail, String password, String email) {
        // Fetch the authenticated user
        User user = entityFetcher.getUserByEmail(email);

        // Validate the password
        String currentPassword = user.getPassword();
        if (!passwordEncoder.matches(password, currentPassword)) {
            throw new InvalidPasswordException("Password does not match");
        }

        //Generate a unique-token for email change verification
        String token = UUID.randomUUID().toString();

        //Set token expiry for this verification
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        //Build and set Verification Entity
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(expiry);
        verificationToken.setNewEmail(newEmail);
        verificationToken.setType(TokenType.EMAIL_CHANGE);

        //Add the new verification in user VerificationToken list
        user.getVerificationTokens().add(verificationToken);

        //Send verification email to the new email address user set
        emailService.sendConfirmationNewEmail(newEmail, token);

        userRepository.save(user);
    }

    public void deleteToken(String email) {
        // Fetch the authenticated user
        User user = entityFetcher.getUserByEmail(email);

        //Generate a random number token for deletion verification
        String token = String.format("%06d", new Random().nextInt(999999));

        //Set token expiry for this verification
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(2);

        //Build and set Verification Entity
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(expiry);
        verificationToken.setType(TokenType.DELETE_ACCOUNT);

        //Add the new verification in user VerificationToken list
        user.getVerificationTokens().add(verificationToken);

        //Send verification token code to user email address
        emailService.sendTokenCodeForDeletion(email, token);

        userRepository.save(user);
    }

    public List<Nimbus> findAllNimbusByUserId(String email) {
        // Fetch the authenticated user
        User user = entityFetcher.getUserByEmail(email);
        Long userId = user.getId();

        return nimbusRepository.findByUserId(userId);
    }
}
