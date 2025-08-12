package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.repository.NimbusRepository;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class NimbusService {
    private final Path STORAGE_ROOT = Paths.get("NimbusSpace");
    private final NimbusRepository nimbusRepository;
    private final UserRepository userRepository;

    public NimbusService(NimbusRepository nimbusRepository, UserRepository userRepository) {
        this.nimbusRepository = nimbusRepository;
        this.userRepository = userRepository;
    }

    public Nimbus createNimbus(String nimbusName, String email) {
         User user = userRepository.findByEmail(email).orElseThrow( () -> new UsernameNotFoundException("User not found: " + email));
         Nimbus nimbus = new Nimbus(nimbusName, user.getNimbusSpace());

         Path nimbusPath = STORAGE_ROOT.resolve(user.getUserDisplayName()).resolve(nimbusName);

         try{
             Files.createDirectories(nimbusPath);
         }catch (IOException e){
             throw new RuntimeException(e.getMessage()+": " + nimbusPath);
         }
         return nimbusRepository.save(nimbus);
    }


}
