package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.entity.*;
import dev.pollywag.nimbusdrop.exception.DropNotFoundException;
import dev.pollywag.nimbusdrop.exception.NimbusNotFoundException;
import dev.pollywag.nimbusdrop.exception.VerificationNotFoundException;
import dev.pollywag.nimbusdrop.repository.DropRepository;
import dev.pollywag.nimbusdrop.repository.NimbusRepository;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import dev.pollywag.nimbusdrop.repository.VerificationTokenRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EntityFetcher {
    private final UserRepository userRepository;
    private final NimbusRepository nimbusRepository;
    private final DropRepository dropRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    public EntityFetcher (UserRepository userRepository, NimbusRepository nimbusRepository,
                          DropRepository dropRepository, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.nimbusRepository = nimbusRepository;
        this.dropRepository = dropRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    // Returns user by email or throws if not found
    public User getUserByEmail(String email){
       return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // Returns verification token by token string or throws if not found
    public VerificationToken getVerificationTokenByToken(String token){
        return verificationTokenRepository.findByToken(token).orElseThrow(() -> new VerificationNotFoundException("Token not found"));
    }

    // Returns verification token by user id and type or throws if not found
    public VerificationToken getVerificationTokenByUserIdAndType(Long userId, TokenType type){

        return verificationTokenRepository.findByUserIdAndType(userId, type).orElseThrow(() -> new VerificationNotFoundException("Token not found"));
    }


    // Returns Nimbus by id or throws if not found
    public Nimbus getNimbusById(Long nimbusId){
        return  nimbusRepository.findById(nimbusId)
                .orElseThrow( () -> new NimbusNotFoundException("Nimbus not found: " + nimbusId));
    }

    // Returns Drop by id or throws if not found
    public Drop getDropById(Long dropId){
        return dropRepository.findById(dropId).orElseThrow(() -> new DropNotFoundException("Drop not found: " + dropId));
    }
}
