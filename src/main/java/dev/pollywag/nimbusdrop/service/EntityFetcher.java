package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.entity.Drop;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.entity.VerificationToken;
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

    public User getUserByEmail(String email){
       return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public VerificationToken getVerificationTokenByToken(String token){
        return verificationTokenRepository.findByToken(token).orElseThrow(() -> new VerificationNotFoundException("Token not found"));
    }

    public Nimbus getNimbusById(Long nimbusId){
        return  nimbusRepository.findById(nimbusId)
                .orElseThrow( () -> new NimbusNotFoundException("Nimbus not found: " + nimbusId));
    }

    public Drop getDropById(Long dropId){
        return dropRepository.findById(dropId).orElseThrow(() -> new DropNotFoundException("Drop not found: " + dropId));
    }
}
