package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.AuthResponse;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.entity.VerificationToken;
import dev.pollywag.nimbusdrop.exception.ResourceOwnershipException;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import dev.pollywag.nimbusdrop.repository.VerificationTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final FileStorageService fileStorageService;

    public VerificationService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository
    , FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.fileStorageService = fileStorageService;
    }

    public String signUpConfirmation(String token){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElseThrow(()-> new RuntimeException("Token not found"));

        if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Token is expired");
        }

        if(verificationToken.isUsed()){
            throw new RuntimeException("Token is already used");
        }
        String userEmail = verificationToken.getUser().getEmail();
        User user = userRepository.findByEmail(userEmail).orElseThrow(()-> new RuntimeException("User not found"));

        verificationToken.setUsed(true);
        user.setEnabled(true);

        userRepository.save(user);
        verificationTokenRepository.save(verificationToken);

        return "Account confirmed. You can now log in.";
    }

    public String newEmailConfirmation(String token){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElseThrow(()-> new RuntimeException("Token not found"));

        if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Token is expired");
        }

        if(verificationToken.isUsed()){
            throw new RuntimeException("Token is already used");
        }


        User user = verificationToken.getUser();
        verificationToken.setUsed(true);
        user.setEmail(verificationToken.getNewEmail());

        userRepository.save(user);
        verificationTokenRepository.save(verificationToken);

        return "Email confirmed. You can now log in to your new email.";
    }

    public String confirmTokenDeletionAccount(Long userId, String token, String email){
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if(!user.getUserDisplayName().equals(userDisplayName)) {
            throw new ResourceOwnershipException("You are not allowed to delete this user");
        }

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElseThrow(()-> new RuntimeException("Token not found"));

        if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Token is expired");
        }

        if(verificationToken.isUsed()){
            throw new RuntimeException("Token is already used");
        }

        user = verificationToken.getUser();

        String userRootStorage = "user_"+user.getId();

        if(!user.getNimbusSpace().getNimbus().isEmpty()){
            fileStorageService.deleteUserDropFolder(userRootStorage);
        }

        verificationTokenRepository.delete(verificationToken);
        userRepository.delete(user);

        return "Successfully deleted account";
    }





}
