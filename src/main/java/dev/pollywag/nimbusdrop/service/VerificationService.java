package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.AuthResponse;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.entity.VerificationToken;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import dev.pollywag.nimbusdrop.repository.VerificationTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    public VerificationService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public String signUpConfirmation(String token){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElseThrow(()-> new RuntimeException("Token not found"));

        if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Token is expired");
        }
        String userEmail = verificationToken.getUser().getEmail();
        User user = userRepository.findByEmail(userEmail).orElseThrow(()-> new RuntimeException("User not found"));

        verificationToken.setUsed(true);
        user.setEnabled(true);

        userRepository.save(user);
        verificationTokenRepository.save(verificationToken);

        return "Account confirmed. You can now log in.";
    }



}
