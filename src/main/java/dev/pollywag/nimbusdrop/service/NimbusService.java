package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.NimbusResponse;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.entity.NimbusSpace;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.exception.NimbusNotFoundException;
import dev.pollywag.nimbusdrop.exception.ResourceOwnershipException;
import dev.pollywag.nimbusdrop.repository.DropRepository;
import dev.pollywag.nimbusdrop.repository.NimbusRepository;
import dev.pollywag.nimbusdrop.repository.NimbusSpaceRepository;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOException;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class NimbusService {

    private final NimbusRepository nimbusRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;
    private final DropRepository dropRepository;
    private final NimbusSpaceRepository nimbusSpaceRepository;

    public NimbusService(NimbusSpaceRepository nimbusSpaceRepository,DropRepository dropRepository,NimbusRepository nimbusRepository, UserRepository userRepository, ModelMapper modelMapper, FileStorageService fileStorageService) {
        this.nimbusRepository = nimbusRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.fileStorageService = fileStorageService;
        this.dropRepository = dropRepository;
        this.nimbusSpaceRepository = nimbusSpaceRepository;
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
        String nimbusOwnerUsername =nimbus.getNimbusSpace().getUser().getUserDisplayName();

        if(!userDisplayName.equals(nimbusOwnerUsername)){
            throw new ResourceOwnershipException("You are not allowed to modify this resource");
        }

        if(!nimbus.getDrops().isEmpty()){
            throw new IllegalArgumentException("All Drops must be empty for this nimbus before deletion");
        }

        String nimbusName = nimbus.getNimbusName();
        fileStorageService.deleteNimbusDirectory(userDisplayName, nimbusName);
        nimbusRepository.delete(nimbus);
    }

    public String emptyNimbus(Long id, String email){
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow( () -> new UsernameNotFoundException("Account not found: " + email));
        Nimbus nimbus = nimbusRepository.findById(id).orElseThrow( () -> new NimbusNotFoundException("Nimbus not found: " + id));
        NimbusSpace nimbusSpace = nimbus.getNimbusSpace();
        String nimbusOwnerUsername =nimbus.getNimbusSpace().getUser().getUserDisplayName();

        if(!userDisplayName.equals(nimbusOwnerUsername)){
            throw new ResourceOwnershipException("You are not allowed to modify this resource");
        }

        if(nimbus.getDrops().isEmpty()){
            throw new IllegalArgumentException("This nimbus is already empty");
        }

        String nimbusName = nimbus.getNimbusName();

        fileStorageService.emptyNimbusDirectory(userDisplayName,nimbusName);
        nimbusSpace.setUsedStorageBytes(0L);

        nimbusSpaceRepository.save(nimbusSpace);
        int totalOfDropsDeleted = dropRepository.deleteAllByNimbusId(id);

        return totalOfDropsDeleted + " drops were deleted";
    }

    public NimbusResponse updateNimbusName(Long id, String newNimbusName, String email){
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow( () -> new UsernameNotFoundException("Account not found: " + email));
        Nimbus nimbus = nimbusRepository.findById(id).orElseThrow( () -> new NimbusNotFoundException("Nimbus not found: " + id));

        String nimbusOwnerUsername =nimbus.getNimbusSpace().getUser().getUserDisplayName();
        String oldNimbusName = nimbus.getNimbusName();

        if(!userDisplayName.equals(nimbusOwnerUsername)){
            throw new ResourceOwnershipException("You are not allowed to modify this resource");
        }

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
