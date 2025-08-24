package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.NimbusResponse;
import dev.pollywag.nimbusdrop.entity.*;
import dev.pollywag.nimbusdrop.exception.DropNotFoundException;
import dev.pollywag.nimbusdrop.exception.NimbusNotFoundException;
import dev.pollywag.nimbusdrop.exception.ResourceOwnershipException;
import dev.pollywag.nimbusdrop.repository.*;
import dev.pollywag.nimbusdrop.util.CheckOwnerUtil;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
    private final EntityFetcher entityFetcher;

    public NimbusService(NimbusSpaceRepository nimbusSpaceRepository, DropRepository dropRepository, NimbusRepository nimbusRepository,
                         UserRepository userRepository, ModelMapper modelMapper, EntityFetcher entityFetcher, FileStorageService fileStorageService
                         , DropShareLinkRepository dropShareLinkRepository) {
        this.nimbusRepository = nimbusRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.fileStorageService = fileStorageService;
        this.dropRepository = dropRepository;
        this.nimbusSpaceRepository = nimbusSpaceRepository;
        this.dropShareLinkRepository = dropShareLinkRepository;
        this.entityFetcher = entityFetcher;
    }

    public Nimbus createNimbus(String nimbusName, String email) {
         // Fetch authenticated user by email
         User user = entityFetcher.getUserByEmail(email);

         // Fetch Nimbus by its ID
         Nimbus nimbus = new Nimbus();
         nimbus.setUser(user);
         nimbus.setNimbusName(nimbusName);

         return nimbusRepository.save(nimbus);
    }

    public Nimbus getNimbusById(Long nimbusId, String email) {
        // Fetch authenticated user by email
        User user = entityFetcher.getUserByEmail(email);

        // Fetch Nimbus by its ID
        Nimbus nimbus = entityFetcher.getNimbusById(nimbusId);

        //Only the Nimbus owner can get this nimbus
        if(CheckOwnerUtil.checkNimbusOwnerValidity(nimbus, user)) {
            throw new ResourceOwnershipException("You are not allowed to get this nimbus");
        }

        return nimbus;
    }

    public List<Drop> getAllDropByNimbusId(Long nimbusId, String email) {
        // Fetch authenticated user by email
        User user = entityFetcher.getUserByEmail(email);

        // Fetch Nimbus by its ID
        Nimbus nimbus = entityFetcher.getNimbusById(nimbusId);

        //Check the Ownership
        if(CheckOwnerUtil.checkNimbusOwnerValidity(nimbus, user)){
            throw new ResourceOwnershipException("You are not allowed to get this drops from this nimbus");
        }


        return dropRepository.findByNimbusId(nimbusId);
    }

    public void deleteNimbus(Long nimbusId, String email){
        // Fetch authenticated user by email
        User user = entityFetcher.getUserByEmail(email);

        // Fetch Nimbus by its ID
        Nimbus nimbus = entityFetcher.getNimbusById(nimbusId);

        //Fetch the NimbusSpace of User
        NimbusSpace nimbusSpace = user.getNimbusSpace();

        //Check the Ownership
        if(CheckOwnerUtil.checkNimbusOwnerValidity(nimbus, user)){
            throw new ResourceOwnershipException("You are not allowed to delete this nimbus");
        }

        //Check if Nimbus is already empty
        if(!nimbus.getDrops().isEmpty()){
            throw new IllegalArgumentException("All Drops must be empty for this nimbus before deletion");
        }

        //Path of Nimbus Folder to be deleted
        String nimbusPath = ("user_" + user.getId() + "/nimbus_" + nimbus.getId());

        //Service to delete the folder of nimbus by path
        fileStorageService.deleteNimbusDirectory(nimbusPath);

        nimbusRepository.delete(nimbus);
    }

    public void emptyNimbus(Long nimbusId, String email){
        // Fetch authenticated user by email
        User user = entityFetcher.getUserByEmail(email);

        // Fetch Nimbus by its ID
        Nimbus nimbus = entityFetcher.getNimbusById(nimbusId);

        //Fetch the NimbusSpace of User
        NimbusSpace nimbusSpace = user.getNimbusSpace();

        if(CheckOwnerUtil.checkNimbusOwnerValidity(nimbus, user)){
            throw new ResourceOwnershipException("You are not allowed to empty this nimbus");
        }

        if(nimbus.getDrops().isEmpty()){
            throw new IllegalArgumentException("This nimbus is already empty");
        }

        String nimbusPath = ("user_" + user.getId() + "/nimbus_" + nimbus.getId());

        fileStorageService.emptyNimbusDirectory(nimbusPath);

        nimbusSpace.setUsedStorageBytes(0L);

        nimbusSpaceRepository.save(nimbusSpace);

        dropRepository.deleteAllByNimbusId(nimbusId);
    }

    public Nimbus updateNimbusName(Long nimbusId, String newNimbusName, String email){
        // Fetch authenticated user by email
        User user = entityFetcher.getUserByEmail(email);

        // Fetch Nimbus by its ID
        Nimbus nimbus = entityFetcher.getNimbusById(nimbusId);

        if(CheckOwnerUtil.checkNimbusOwnerValidity(nimbus, user)){
            throw new ResourceOwnershipException("You are not allowed to modify this nimbus");
        }

        nimbus.setNimbusName(newNimbusName);

        return nimbusRepository.save(nimbus);
    }

    public String createShareLink(Long dropId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow( () -> new UsernameNotFoundException("User not found: " + email));
        Drop drop = dropRepository.findById(dropId).orElseThrow(()->new DropNotFoundException("Drop not found"));

        if(CheckOwnerUtil.checkDropOwnerValidity(drop, user)) {
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
