package dev.pollywag.nimbusdrop.controller;

import dev.pollywag.nimbusdrop.dto.requestDTO.AccountDeletionRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.ChangeEmailRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.ChangePasswordRequest;
import dev.pollywag.nimbusdrop.dto.requestDTO.ChangeUsernameRequest;
import dev.pollywag.nimbusdrop.dto.respondeDTO.ApiResponse;
import dev.pollywag.nimbusdrop.dto.respondeDTO.NimbusResponse;
import dev.pollywag.nimbusdrop.dto.respondeDTO.UserResponse;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.service.UserService;
import dev.pollywag.nimbusdrop.service.VerificationService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final VerificationService verificationService;
    private final ModelMapper modelMapper;
    public UserController(UserService userService, VerificationService verificationService, ModelMapper modelMapper) {
        this.userService = userService;
        this.verificationService = verificationService;
        this.modelMapper = modelMapper;
    }


    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(Principal principal) {
        String email = principal.getName();
        User user = userService.getUser(email);
        UserResponse response = modelMapper.map(user, UserResponse.class);

        return ResponseEntity.ok(ApiResponse.success("Successfully get the user", response));
    }


    @PostMapping("/username")
    public ResponseEntity<ApiResponse<UserResponse>> changeUsername(@Valid @RequestBody ChangeUsernameRequest request, Principal principal) {
        String newUsername = request.getNewUsername();
        String email = principal.getName();

        User updateUser = userService.changeUsername(newUsername, email);
        UserResponse response = modelMapper.map(updateUser, UserResponse.class);

        return ResponseEntity.ok(ApiResponse.success("Username changed successfully", response));
    }

    @PostMapping("/password")
    public ResponseEntity<ApiResponse<?>> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        String newPassword = request.getNewPassword();
        String oldPassword = request.getOldPassword();
        String email = principal.getName();

        userService.changePassword(newPassword, oldPassword, email);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }


    @PostMapping("/email")
    public ResponseEntity<ApiResponse<?>> changeEmail (@Valid @RequestBody ChangeEmailRequest request, Principal principal ) {
        String newEmail = request.getNewEmail();
        String password = request.getPassword();
        String email = principal.getName();

        userService.changeEmail(newEmail, password, email);

        return ResponseEntity.ok(ApiResponse.success("Check you email to verify your new email address"));
    }

    @GetMapping("/token/delete")
    public ResponseEntity<ApiResponse<?>> requestDeleteToken (Principal principal ) {
        String email = principal.getName();
        userService.deleteToken(email);

        return ResponseEntity.ok(ApiResponse.success("Check you email to get your token"));
    }

    @PostMapping("/account/delete/confirm")
    public ResponseEntity<ApiResponse<String>> confirmDeleteAccount (@Valid @RequestBody AccountDeletionRequest request, Principal principal ) {
        String token = request.getToken();
        String email = principal.getName();

        verificationService.confirmTokenDeletionAccount(token, email);

        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }


    @GetMapping("/nimbus")
    public ResponseEntity<ApiResponse<List<NimbusResponse>>> getAllNimbusOfUser (Principal principal) {
        String email = principal.getName();

        List<Nimbus> userNimbus = userService.findAllNimbusByUserId(email);

        List<NimbusResponse> responseList = userNimbus.stream()
                .map(nimbus -> modelMapper.map(nimbus, NimbusResponse.class))
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Retrieved all nimbus", responseList));
    }






}
