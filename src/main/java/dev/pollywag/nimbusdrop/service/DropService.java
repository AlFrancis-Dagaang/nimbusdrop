package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.DropResponse;
import dev.pollywag.nimbusdrop.entity.Drop;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.entity.NimbusSpace;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.exception.DropNotFoundException;
import dev.pollywag.nimbusdrop.exception.NimbusNotFoundException;
import dev.pollywag.nimbusdrop.exception.ResourceOwnershipException;
import dev.pollywag.nimbusdrop.exception.UploadQuotaException;
import dev.pollywag.nimbusdrop.repository.DropRepository;
import dev.pollywag.nimbusdrop.repository.NimbusRepository;
import dev.pollywag.nimbusdrop.repository.NimbusSpaceRepository;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DropService {
    private final DropRepository dropRepository;
    private final QuotaService quotaService;
    private final NimbusRepository nimbusRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final NimbusSpaceRepository nimbusSpaceRepository;

    public DropService(DropRepository dropRepository, QuotaService quotaService,
                       NimbusRepository nimbusRepository, FileStorageService fileStorageService
    , UserRepository userRepository, ModelMapper modelMapper, NimbusSpaceRepository nimbusSpaceRepository) {
        this.dropRepository = dropRepository;
        this.quotaService = quotaService;
        this.nimbusRepository = nimbusRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.nimbusSpaceRepository = nimbusSpaceRepository;
    }

    public Drop getDropById(Long dropId) {
        return dropRepository.findById(dropId).orElseThrow(()-> new DropNotFoundException("Drop not found"));
    }

    public DropResponse uploadDrop(Long nimbusId, MultipartFile file, String email) {
        //Get the user for user displayName
        User user = this.userRepository.findByEmail(email).orElseThrow(() -> new NimbusNotFoundException(email));

        //Check Upload quota
        Nimbus nimbus = nimbusRepository.findNimbusById(nimbusId).orElseThrow(() -> new NimbusNotFoundException("Nimbus not found"));
        String nimbusOwner = nimbus.getNimbusSpace().getUser().getUserDisplayName();

        if(!user.getUserDisplayName().equals(nimbusOwner)) {
            throw new ResourceOwnershipException("You are not the owner of this Nimbus");
        }

        if (!quotaService.canUpload(nimbus.getNimbusSpace().getId())) {
            throw new UploadQuotaException("Upload quota exceeded");
        }

        if(!quotaService.canUploadByStorage(nimbus.getNimbusSpace().getId(), file.getSize())) {
            throw new UploadQuotaException("Max Storage exceeded");
        }

        String dropKey = "user_"+user.getId()+"/nimbus_"+nimbus.getId()+"/"+ UUID.randomUUID()+"-"+file.getOriginalFilename();

        try {//Store Drop File
            fileStorageService.saveDropFile(dropKey, file);
        }catch (IOException e){
            throw new RuntimeException("Failed to save drop file", e);
        }
        //Create Drop entity
        Drop drop = new Drop();
        drop.setDropName(file.getOriginalFilename());
        drop.setContentType(file.getContentType());
        drop.setSize(file.getSize());
        drop.setNimbus(nimbus);
        drop.setUploadedAt(LocalDateTime.now());
        drop.setDropKey(dropKey);

        //Save Nimbus and Drop
        nimbus.getDrops().add(drop);
        nimbusRepository.save(nimbus);

        //Register upload for quota tracking
        quotaService.registerUpload(nimbus.getNimbusSpace().getId(), file.getSize());

        return modelMapper.map(drop, DropResponse.class);
    }

    public void deleteDrop(Long dropId, String email) throws IOException {
        Drop drop = dropRepository.findById(dropId).orElseThrow(() -> new NimbusNotFoundException("Drop not found"));
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow(() -> new NimbusNotFoundException(email));
        String dropOwnerUserDisplayName = drop.getNimbus().getNimbusSpace().getUser().getUserDisplayName();

        if(!userDisplayName.equals(dropOwnerUserDisplayName)){
            throw new ResourceOwnershipException("You are not allowed to delete this drop");
        }

        NimbusSpace nimbusSpace = drop.getNimbus().getNimbusSpace();

        String dropKey = drop.getDropKey();

        fileStorageService.deleteDrop(dropKey);

        Long updateStorageBytes = nimbusSpace.getUsedStorageBytes() - drop.getSize();
        nimbusSpace.setUsedStorageBytes(updateStorageBytes);

        nimbusSpaceRepository.save(nimbusSpace);
        dropRepository.delete(drop);
    }

    public Resource openDrop(Long dropId, String email) throws MalformedURLException {
        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow(() -> new NimbusNotFoundException(email));
        Drop drop = dropRepository.findById(dropId).orElseThrow(() -> new NimbusNotFoundException("Drop not found"));
        String dropOwnerUserDisplayName = drop.getNimbus().getNimbusSpace().getUser().getUserDisplayName();
        Long nimbusSpaceId = drop.getNimbus().getNimbusSpace().getId();
        if(!userDisplayName.equals(dropOwnerUserDisplayName)){
            throw new ResourceOwnershipException("You are not allowed to access this drop");
        }

        if(!quotaService.canView(nimbusSpaceId)) {
            throw new UploadQuotaException("Maximum view for this drop exceeded");
        }

        String dropKey = drop.getDropKey();

        quotaService.registerView(nimbusSpaceId);

        return fileStorageService.openDropFile(dropKey);
    }


    public Resource downloadDropFile(String dropKey, String email, Long userId) throws IOException {

        String userDisplayName = userRepository.findUserDisplayNameByEmail(email).orElseThrow(() -> new NimbusNotFoundException(email));
        User user = userRepository.findById(userId).orElseThrow(() -> new NimbusNotFoundException("User not found"));
        Long nimbusSpaceId = user.getNimbusSpace().getId();
        if(!user.getUserDisplayName().equals(userDisplayName)){
            throw new ResourceOwnershipException("You are not allowed to download this drop");
        }

        if(!quotaService.canDownload(nimbusSpaceId)) {
            throw new UploadQuotaException("Maximum download for this drop exceeded");
        }

        quotaService.registerDownload(nimbusSpaceId);

        return fileStorageService.dowloadDropFile(dropKey);
    }
}
