package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.NimbusResponse;
import dev.pollywag.nimbusdrop.entity.*;
import dev.pollywag.nimbusdrop.exception.DropNotFoundException;
import dev.pollywag.nimbusdrop.exception.NimbusNotFoundException;
import dev.pollywag.nimbusdrop.exception.ResourceOwnershipException;
import dev.pollywag.nimbusdrop.repository.*;
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
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NimbusService {

    private final NimbusRepository nimbusRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;
    private final DropRepository dropRepository;
    private final NimbusSpaceRepository nimbusSpaceRepository;
    private final DropShareLinkRepository dropShareLinkRepository;

    public NimbusService(NimbusSpaceRepository nimbusSpaceRepository,DropRepository dropRepository,NimbusRepository nimbusRepository,
                         UserRepository userRepository, ModelMapper modelMapper,
                         FileStorageService fileStorageService, DropShareLinkRepository dropShareLinkRepository) {
        this.nimbusRepository = nimbusRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.fileStorageService = fileStorageService;
        this.dropRepository = dropRepository;
        this.nimbusSpaceRepository = nimbusSpaceRepository;
        this.dropShareLinkRepository = dropShareLinkRepository;
    }

    public NimbusResponse createNimbus(String nimbusName, String email) {
         User user = userRepository.findByEmail(email).orElseThrow( () -> new UsernameNotFoundException("User not found: " + email));
         Nimbus nimbus = new Nimbus(nimbusName, user.getNimbusSpace());

         nimbusRepository.save(nimbus);
         return modelMapper.map(nimbus, NimbusResponse.class);
    }

    public void deleteNimbus(Long id, String email){
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow( () -> new UsernameNotFoundException("Account not found: " + email));
        Nimbus nimbus = nimbusRepository.findById(id).orElseThrow( () -> new NimbusNotFoundException("Nimbus not found: " + id));
        String nimbusOwnerUsername = nimbus.getNimbusSpace().getUser().getUserDisplayName();
        NimbusSpace nimbusSpace = nimbus.getNimbusSpace();

        if(!userDisplayName.equals(nimbusOwnerUsername)){
            throw new ResourceOwnershipException("You are not allowed to delete this nimbus");
        }

        if(!nimbus.getDrops().isEmpty()){
            throw new IllegalArgumentException("All Drops must be empty for this nimbus before deletion");
        }

        String nimbusPath = ("user_" + nimbusSpace.getUser().getId() + "/nimbus_" + nimbus.getId());

        fileStorageService.deleteNimbusDirectory(nimbusPath);

        nimbusRepository.delete(nimbus);
    }

    public String emptyNimbus(Long id, String email){
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow( () -> new UsernameNotFoundException("Account not found: " + email));
        Nimbus nimbus = nimbusRepository.findById(id).orElseThrow( () -> new NimbusNotFoundException("Nimbus not found: " + id));
        NimbusSpace nimbusSpace = nimbus.getNimbusSpace();
        String nimbusOwnerUsername =nimbus.getNimbusSpace().getUser().getUserDisplayName();

        if(!userDisplayName.equals(nimbusOwnerUsername)){
            throw new ResourceOwnershipException("You are not allowed to empty this nimbus");
        }

        if(nimbus.getDrops().isEmpty()){
            throw new IllegalArgumentException("This nimbus is already empty");
        }

        String nimbusPath = ("user_" + nimbusSpace.getUser().getId() + "/nimbus_" + nimbus.getId());

        fileStorageService.emptyNimbusDirectory(nimbusPath);

        nimbusSpace.setUsedStorageBytes(0L);

        nimbusSpaceRepository.save(nimbusSpace);
        int totalOfDropsDeleted = dropRepository.deleteAllByNimbusId(id);

        return totalOfDropsDeleted + " drops were deleted";
    }

    public NimbusResponse updateNimbusName(Long id, String newNimbusName, String email){
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow( () -> new UsernameNotFoundException("Account not found: " + email));
        Nimbus nimbus = nimbusRepository.findById(id).orElseThrow( () -> new NimbusNotFoundException("Nimbus not found: " + id));

        String nimbusOwnerUsername =nimbus.getNimbusSpace().getUser().getUserDisplayName();

        if(!userDisplayName.equals(nimbusOwnerUsername)){
            throw new ResourceOwnershipException("You are not allowed to modify this nimbus");
        }

        nimbus.setNimbusName(newNimbusName);
        nimbus = nimbusRepository.save(nimbus);

        return modelMapper.map(nimbus, NimbusResponse.class);
    }

    public String createShareLink(Long dropId, String email) {

        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow(() -> new DropNotFoundException(email));

        Drop drop = dropRepository.findById(dropId).orElseThrow(()->new DropNotFoundException("Drop not found"));

        String dropUserDisplayName = drop.getNimbus().getNimbusSpace().getUser().getUserDisplayName();

        if(!userDisplayName.equals(dropUserDisplayName)) {
            throw new ResourceOwnershipException("You cant create a shared link for this drop");
        }

        String token = UUID.randomUUID().toString();

        DropSharedLink dropSharedLink = new DropSharedLink();
        dropSharedLink.setToken(token);
        dropSharedLink.setDropId(drop.getId());
        dropSharedLink.setExpiresAt(LocalDateTime.now().plusMinutes(3));

        dropShareLinkRepository.save(dropSharedLink);

        return "http://localhost:8085/public/" + token;
    }







}
