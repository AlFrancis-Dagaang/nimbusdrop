package dev.pollywag.nimbusdrop.controller;

import dev.pollywag.nimbusdrop.dto.requestDTO.AccountDeletionRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.ChangeEmailRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.ChangePasswordRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.ChangeUsernameRequest;
import dev.pollywag.nimbusdrop.dto.respondeDTO.ApiResponse;
import dev.pollywag.nimbusdrop.dto.respondeDTO.UserResponse;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.service.UserService;
import dev.pollywag.nimbusdrop.service.VerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final VerificationService verificationService;
    public UserController(UserService userService, VerificationService verificationService) {
        this.userService = userService;
        this.verificationService = verificationService;
    }

    @PostMapping("/{userId}/username")
    public ResponseEntity<ApiResponse<UserResponse>> changeUsername(@PathVariable Long userId, @RequestBody ChangeUsernameRequest request, Principal principal) {
        UserResponse response = userService.changeUsername(userId, request.getNewUsername(), principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User changed successfully", response));
    }
    @PostMapping("/{userId}/password")
    public ResponseEntity<ApiResponse<String>> changePassword(@PathVariable Long userId, @RequestBody ChangePasswordRequest request, Principal principal) {
        String response = userService.changePassword(userId, request.getNewPassword(), request.getOldPassword(), principal.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{userId}/email")
    public ResponseEntity<ApiResponse<String>> changeEmail (@PathVariable Long userId, @RequestBody ChangeEmailRequest request, Principal principal ) {
        String response = userService.changeEmail(userId, request.getNewEmail(), request.getPassword(), principal.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}/delete-token")
    public ResponseEntity<ApiResponse<String>> requestDeleteToken (@PathVariable Long userId, Principal principal ) {
        String response = userService.deleteToken(userId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{userId}/confirm-delete")
    public ResponseEntity<ApiResponse<String>> confirmDeleteAccount (@PathVariable Long userId, @RequestBody AccountDeletionRequest request, Principal principal ) {
        String response = verificationService.confirmTokenDeletionAccount(userId, request.getToken(), principal.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }






}
