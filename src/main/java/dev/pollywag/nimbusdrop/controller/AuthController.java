package dev.pollywag.nimbusdrop.controller;

import dev.pollywag.nimbusdrop.dto.respondeDTO.ApiResponse;
import dev.pollywag.nimbusdrop.dto.respondeDTO.AuthResponse;
import dev.pollywag.nimbusdrop.dto.requestDTO.LoginRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.SignupRequest;
import dev.pollywag.nimbusdrop.entity.Role;
import dev.pollywag.nimbusdrop.service.AuthService;
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

    public AuthController(AuthService authService, VerificationService verificationService) {
        this.authService = authService;
        this.verificationService = verificationService;
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

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logout successful", "Please remove the token from client storage"));
    }

    @GetMapping("/confirm")
    public ResponseEntity<ApiResponse<String>> confirmSignupVerification(@Param("token") String token) {
        verificationService.signUpConfirmation(token);
        return ResponseEntity.ok(ApiResponse.success("Successfully confirmed account"));
    }

    @GetMapping("/email")
    public ResponseEntity<ApiResponse<String>> confirmEmailVerification(@Param("token") String token) {
        verificationService.newEmailConfirmation(token);
        return ResponseEntity.ok(ApiResponse.success("Email confirmed. You can now log in to your new email."));
    }

}