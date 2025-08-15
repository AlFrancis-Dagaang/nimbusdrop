package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.NimbusResponse;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.exception.NimbusNotFoundException;
import dev.pollywag.nimbusdrop.repository.NimbusRepository;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class NimbusService {

    private final NimbusRepository nimbusRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;

    public NimbusService(NimbusRepository nimbusRepository, UserRepository userRepository, ModelMapper modelMapper, FileStorageService fileStorageService) {
        this.nimbusRepository = nimbusRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.fileStorageService = fileStorageService;
    }

    public NimbusResponse createNimbus(String nimbusName, String email) {
         User user = userRepository.findByEmail(email).orElseThrow( () -> new UsernameNotFoundException("User not found: " + email));
         Nimbus nimbus = new Nimbus(nimbusName, user.getNimbusSpace());

         String userDisplayName = user.getUserDisplayName();
         fileStorageService.createNimbusDirectory(userDisplayName, nimbusName);
         nimbusRepository.save(nimbus);

         return modelMapper.map(nimbus, NimbusResponse.class);
    }

    public void deleteNimbus(Long id, String email){
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow( () -> new UsernameNotFoundException("Account not found: " + email));
        Nimbus nimbus = nimbusRepository.findById(id).orElseThrow( () -> new NimbusNotFoundException("Nimbus not found: " + id));

        String nimbusName = nimbus.getNimbusName();
        fileStorageService.deleteNimbusDirectory(userDisplayName, nimbusName);
        nimbusRepository.delete(nimbus);
    }

    public NimbusResponse updateNimbusName(Long id, String newNimbusName){
        Nimbus nimbus = nimbusRepository.findById(id).orElseThrow( () -> new NimbusNotFoundException("Nimbus not found: " + id));
        User user = nimbus.getNimbusSpace().getUser();

        String userDisplayName = user.getUserDisplayName();
        String oldNimbusName = nimbus.getNimbusName();

        try {
            fileStorageService.nimbusRename(userDisplayName, newNimbusName, oldNimbusName);
        }catch (IOException e){
            throw new RuntimeException("Failed to rename nimbus file");
        }

        nimbus.setNimbusName(newNimbusName);
        nimbus = nimbusRepository.save(nimbus);

        return modelMapper.map(nimbus, NimbusResponse.class);
    }




}
