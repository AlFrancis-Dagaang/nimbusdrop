package dev.pollywag.nimbusdrop.controller;

import dev.pollywag.nimbusdrop.dto.requestDTO.ResendVerificationEmailRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.PasswordForgotRequest;
import dev.pollywag.nimbusdrop.dto.respondeDTO.ApiResponse;
import dev.pollywag.nimbusdrop.dto.respondeDTO.AuthResponse;
import dev.pollywag.nimbusdrop.dto.requestDTO.LoginRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.SignupRequest;
import dev.pollywag.nimbusdrop.entity.Role;
import dev.pollywag.nimbusdrop.service.AuthService;
import dev.pollywag.nimbusdrop.service.UserService;
import dev.pollywag.nimbusdrop.service.VerificationService;
import jakarta.validation.Valid;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")

public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;
    private final UserService userService;

    public AuthController(AuthService authService, VerificationService verificationService, UserService userService) {
        this.authService = authService;
        this.verificationService = verificationService;
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signup(@Valid @RequestBody SignupRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        String username = request.getUsername();
        Role role = request.getRole();

        authService.signup(email, username, password, role);

        return ResponseEntity.ok(ApiResponse.success("Please check your email to confirm your account."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        AuthResponse response = authService.authenticate(email, password);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        String token = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
        AuthResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }


    @GetMapping("/confirm")
    public ResponseEntity<ApiResponse<?>> confirmSignupVerification(@Param("token") String token) {
        authService.signUpConfirmation(token);
        return ResponseEntity.ok(ApiResponse.success("Successfully confirmed account"));
    }

    @GetMapping("/new-email")
    public ResponseEntity<ApiResponse<String>> confirmNewEmailVerification(@Param("token") String token) {
        verificationService.newEmailConfirmation(token);
        return ResponseEntity.ok(ApiResponse.success("Email confirmed. You can now log in to your new email."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<?>> resendVerificationEmail(@RequestBody ResendVerificationEmailRequest request) {
        String email = request.getEmail();
        authService.resendEmailVerificationToken(email);
        return ResponseEntity.ok(ApiResponse.success("Resend verification email successful"));
    }

    //---- PASSWORD-FORGOT API SECTION -----//
    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendTokenCodeForPasswordForgot(@RequestBody PasswordForgotRequest request){
        String email = request.getEmail();
        authService.createForgotPasswordToken(email);
        return ResponseEntity.ok(ApiResponse.success("Please check your email. A verification link was sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordForgotRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        authService.setNewUserPassword(token, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful"));
    }




}