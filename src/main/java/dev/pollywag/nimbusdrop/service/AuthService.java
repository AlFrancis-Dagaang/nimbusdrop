package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.AuthResponse;
import dev.pollywag.nimbusdrop.dto.requestDTO.LoginRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.SignupRequest;
import dev.pollywag.nimbusdrop.entity.Role;
import dev.pollywag.nimbusdrop.entity.TokenType;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.entity.VerificationToken;
import dev.pollywag.nimbusdrop.exception.TokenRefreshException;
import dev.pollywag.nimbusdrop.exception.UserAlreadyExistsException;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import dev.pollywag.nimbusdrop.repository.VerificationTokenRepository;
import dev.pollywag.nimbusdrop.security.jwt.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager
    , VerificationTokenRepository verificationTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
    }

    public String signup(SignupRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        // Create new user
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getUsername(),
                request.getRole() != null ? request.getRole() : Role.USER
        );

        user.setEnabled(false);


        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(expiry);
        verificationToken.setType(TokenType.SIGNUP_CONFIRM);
        user.getVerificationTokens().add(verificationToken);

        userRepository.save(user);

        emailService.sendConfirmationEmail(user.getEmail(), token);


        return "Please check your email to confirm your account.";
    }

    public AuthResponse authenticate(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(!user.getEnabled()){
            throw new RuntimeException("User is not enabled");
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return createAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new TokenRefreshException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new TokenRefreshException("Refresh token is expired or invalid");
        }

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return createAuthResponse(user, newAccessToken, newRefreshToken);
    }

    private AuthResponse createAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name()
        );

        return new AuthResponse(
                accessToken,
                refreshToken,
                userInfo,
                jwtService.getExpirationTime()
        );
    }
}