package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.AuthResponse;
import dev.pollywag.nimbusdrop.dto.requestDTO.LoginRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.SignupRequest;
import dev.pollywag.nimbusdrop.dto.respondeDTO.UserResponse;
import dev.pollywag.nimbusdrop.entity.Role;
import dev.pollywag.nimbusdrop.entity.TokenType;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.entity.VerificationToken;
import dev.pollywag.nimbusdrop.exception.TokenRefreshException;
import dev.pollywag.nimbusdrop.exception.UserAlreadyExistsException;
import dev.pollywag.nimbusdrop.exception.UserNotEnableException;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import dev.pollywag.nimbusdrop.repository.VerificationTokenRepository;
import dev.pollywag.nimbusdrop.security.jwt.JwtService;
import dev.pollywag.nimbusdrop.util.ValidatingVerificationTokenUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.core.Local;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final EntityFetcher entityFetcher;
    private final ModelMapper modelMapper;
    private final VerificationTokenRepository verificationTokenRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager,
                       EmailService emailService, EntityFetcher entityFetcher, ModelMapper modelMapper, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.entityFetcher = entityFetcher;
        this.modelMapper = modelMapper;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public void signup(String email, String username, String password, Role role) {
        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with email: " + email);
        }

        // Create new user
        User user = new User(
                email,
                passwordEncoder.encode(password),
                username,
                role != null ? role : Role.USER
        );

        user.setEnabled(false);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(expiry);
        verificationToken.setType(TokenType.SIGNUP_CONFIRM);
        user.getVerificationTokens().add(verificationToken);

        userRepository.save(user);

        emailService.sendConfirmationEmail(user.getEmail(), token);
    }

    public AuthResponse authenticate(String email, String password) {
        // Authenticate user
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,password));

        // Get user details
        User user = entityFetcher.getUserByEmail(email);

        if(!user.getEnabled()){
            throw new UserNotEnableException("User is not enabled");
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

    public void signUpConfirmation(String token){
        VerificationToken verificationToken = entityFetcher.getVerificationTokenByToken(token);

        LocalDateTime expiryDate = verificationToken.getExpiryDate();
        boolean isUsed = verificationToken.isUsed();

        ValidatingVerificationTokenUtil.validateVerificationToken(expiryDate, isUsed);

        String userEmail = verificationToken.getUser().getEmail();

        User user = entityFetcher.getUserByEmail(userEmail);
        verificationToken.setUsed(true);
        user.setEnabled(true);

        userRepository.save(user);
    }

    public void resendEmailVerificationToken(String email){
        User user = entityFetcher.getUserByEmail(email);
        VerificationToken userVerificationToken = entityFetcher.getVerificationTokenByUserIdAndType(user.getId(), TokenType.SIGNUP_CONFIRM);

        String token = userVerificationToken.getToken();

        //Update the expiry date when resending email verification
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);
        userVerificationToken.setExpiryDate(expiryDate);
        userVerificationToken.setUsed(false);

        verificationTokenRepository.save(userVerificationToken);

        emailService.sendConfirmationEmail(email, token);
    }

    public void createForgotPasswordToken(String email){
        User user = entityFetcher.getUserByEmail(email);
        VerificationToken verificationToken = verificationTokenRepository.findByUserIdAndType(user.getId(), TokenType.PASSWORD_FORGOT).orElse(null);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        if(verificationToken != null){
            verificationToken.setToken(token);
            verificationToken.setExpiryDate(expiry);
            verificationToken.setUsed(false);

            emailService.sendForgotPasswordLink(email, token);

            return;
        }

        VerificationToken newToken = new VerificationToken();

        newToken.setToken(token);
        newToken.setUser(user);
        newToken.setExpiryDate(expiry);
        newToken.setType(TokenType.PASSWORD_FORGOT);
        newToken.setUsed(false);
        verificationTokenRepository.save(newToken);

        emailService.sendForgotPasswordLink(email, token);
    }

    public void setNewUserPassword(String token, String newPassword){
        VerificationToken userVerificationToken = entityFetcher.getVerificationTokenByToken(token);
        LocalDateTime expiryDate = userVerificationToken.getExpiryDate();
        boolean isUsed = userVerificationToken.isUsed();

        ValidatingVerificationTokenUtil.validateVerificationToken(expiryDate, isUsed);

        User user = userVerificationToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userVerificationToken.setUsed(true);

        userRepository.save(user);

    }












    private AuthResponse createAuthResponse(User user, String accessToken, String refreshToken) {

        UserResponse userResponse = modelMapper.map(user, UserResponse.class);

        return new AuthResponse(
                accessToken,
                refreshToken,
                userResponse,
                jwtService.getExpirationTime()
        );
    }
}