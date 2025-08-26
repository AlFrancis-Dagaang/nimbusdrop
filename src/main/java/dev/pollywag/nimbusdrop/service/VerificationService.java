package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.AuthResponse;
import dev.pollywag.nimbusdrop.entity.TokenType;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.entity.VerificationToken;
import dev.pollywag.nimbusdrop.exception.InvalidVerificationTokenException;
import dev.pollywag.nimbusdrop.exception.ResourceOwnershipException;
import dev.pollywag.nimbusdrop.exception.VerificationNotFoundException;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import dev.pollywag.nimbusdrop.repository.VerificationTokenRepository;
import dev.pollywag.nimbusdrop.util.ValidatingVerificationTokenUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final FileStorageService fileStorageService;
    private final EntityFetcher entityFetcher;
    private final EmailService emailService;

    public VerificationService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository
            , FileStorageService fileStorageService, EntityFetcher entityFetcher, EmailService emailService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.fileStorageService = fileStorageService;
        this.entityFetcher = entityFetcher;
        this.emailService = emailService;
    }



    public void newEmailConfirmation(String token){
        VerificationToken verificationToken = entityFetcher.getVerificationTokenByToken(token);

        LocalDateTime expiryDate = verificationToken.getExpiryDate();
        boolean isUsed = verificationToken.isUsed();

        ValidatingVerificationTokenUtil.validateVerificationToken(expiryDate, isUsed);

        User user = verificationToken.getUser();
        verificationToken.setUsed(true);
        user.setEmail(verificationToken.getNewEmail());

        userRepository.save(user);
    }

    public void confirmTokenDeletionAccount(String token, String email){
        User user = entityFetcher.getUserByEmail(email);

        VerificationToken verificationToken = entityFetcher.getVerificationTokenByToken(token);

        LocalDateTime expiryDate = verificationToken.getExpiryDate();
        boolean isUsed = verificationToken.isUsed();

        ValidatingVerificationTokenUtil.validateVerificationToken(expiryDate, isUsed);

        String userRootStorage = "user_"+user.getId();

        fileStorageService.deleteUserDropFolder(userRootStorage);

        userRepository.delete(user);
    }





}
