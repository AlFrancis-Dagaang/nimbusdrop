package dev.pollywag.nimbusdrop.controller;

import dev.pollywag.nimbusdrop.dto.requestDTO.ResendVerificationEmailRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.PasswordForgotRequest;
import dev.pollywag.nimbusdrop.dto.respondeDTO.ApiResponse;
import dev.pollywag.nimbusdrop.dto.respondeDTO.AuthResponse;
import dev.pollywag.nimbusdrop.dto.requestDTO.LoginRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.SignupRequest;
import dev.pollywag.nimbusdrop.dto.respondeDTO.AuthResponseHolder;
import dev.pollywag.nimbusdrop.entity.Role;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.security.jwt.JwtService;
import dev.pollywag.nimbusdrop.service.AuthService;
import dev.pollywag.nimbusdrop.service.UserService;
import dev.pollywag.nimbusdrop.service.VerificationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")

public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

    public AuthController(AuthService authService, VerificationService verificationService,
                          JwtService jwtService, ModelMapper modelMapper) {
        this.authService = authService;
        this.verificationService = verificationService;
        this.jwtService = jwtService;
        this.modelMapper = modelMapper;
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
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        String email = request.getEmail();
        String password = request.getPassword();

        AuthResponseHolder authResponseHolder = authService.authenticate(email, password);

        AuthResponse authResponse = authResponseHolder.getAuthResponse();
        String refreshToken = authResponseHolder.getRefreshToken();

        // Set refresh token in HttpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // set true in production (HTTPS)
                .sameSite("Strict")
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());


        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@CookieValue(name = "refreshToken", required = false)String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Missing refresh token"));
        }

        AuthResponseHolder authResponseHolder = authService.refreshToken(refreshToken);

        AuthResponse authResponse = authResponseHolder.getAuthResponse();
        String newRefreshToken = authResponseHolder.getRefreshToken();

        // Set refresh token in HttpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(false) // set true in production (HTTPS)
                .sameSite("Strict")
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }


    @GetMapping("/confirm")
    public ResponseEntity<ApiResponse<?>> confirmSignupVerification(@Param("token") String token) {
        authService.signUpConfirmation(token);
        return ResponseEntity.ok(ApiResponse.success("Successfully confirmed account"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        // Clear refresh token cookie
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // set true in production (HTTPS)
                .sameSite("Strict")
                .path("/auth/refresh")
                .maxAge(0) // expire immediately
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
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

    @GetMapping("/laugh")
    public ResponseEntity<String> getMe(){
        return ResponseEntity.ok("HHAAHHA");
    }
}