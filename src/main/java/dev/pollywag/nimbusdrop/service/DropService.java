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
import dev.pollywag.nimbusdrop.util.CheckOwnerUtil;
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
    private final EntityFetcher entityFetcher;

    public DropService(DropRepository dropRepository, QuotaService quotaService,
                       NimbusRepository nimbusRepository, FileStorageService fileStorageService
    , UserRepository userRepository, ModelMapper modelMapper, NimbusSpaceRepository nimbusSpaceRepository, EntityFetcher entityFetcher) {
        this.dropRepository = dropRepository;
        this.quotaService = quotaService;
        this.nimbusRepository = nimbusRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.nimbusSpaceRepository = nimbusSpaceRepository;
        this.entityFetcher = entityFetcher;
    }

    public Drop getDropById(Long dropId) {
        return dropRepository.findById(dropId).orElseThrow(()-> new DropNotFoundException("Drop not found"));
    }

    public Drop uploadDrop(Long nimbusId, MultipartFile file, String email) throws IOException {
        User user = entityFetcher.getUserByEmail(email);
        Nimbus nimbus = entityFetcher.getNimbusById(nimbusId);
        NimbusSpace nimbusSpace = user.getNimbusSpace();

        if(CheckOwnerUtil.checkNimbusOwnerValidity(nimbus, user)) {
            throw new ResourceOwnershipException("You are not the owner of this Nimbus");
        }

        if (!quotaService.canUpload(nimbusSpace.getId())) {
            throw new UploadQuotaException("Upload quota exceeded");
        }

        if(!quotaService.canUploadByStorage(nimbusSpace.getId(), file.getSize())) {
            throw new UploadQuotaException("Max Storage exceeded");
        }

        String dropKey = "user_"+user.getId()+"/nimbus_"+nimbus.getId()+"/"+ UUID.randomUUID()+"-"+file.getOriginalFilename();

        fileStorageService.saveDropFile(dropKey, file);

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

        userRepository.save(user);

        //Register upload for quota tracking
        quotaService.registerUpload(nimbusSpace.getId(), file.getSize());

        return drop;
    }

    public void deleteDrop(Long dropId, String email) throws IOException {
        User user = entityFetcher.getUserByEmail(email);
        Drop drop = entityFetcher.getDropById(dropId);
        NimbusSpace nimbusSpace = user.getNimbusSpace();

        if(CheckOwnerUtil.checkDropOwnerValidity(drop, user)){
            throw new ResourceOwnershipException("You are not allowed to delete this drop");
        }

        String dropKey = drop.getDropKey();

        fileStorageService.deleteDrop(dropKey);

        Long updateStorageBytes = nimbusSpace.getUsedStorageBytes() - drop.getSize();
        nimbusSpace.setUsedStorageBytes(updateStorageBytes);

        nimbusSpaceRepository.save(nimbusSpace);
        dropRepository.delete(drop);
    }

    public Resource openDrop(Long dropId, String email) throws MalformedURLException {
        User user = entityFetcher.getUserByEmail(email);
        Drop drop = entityFetcher.getDropById(dropId);
        NimbusSpace nimbusSpace = user.getNimbusSpace();

        if(CheckOwnerUtil.checkDropOwnerValidity(drop, user)){
            throw new ResourceOwnershipException("You are not allowed to access this drop");
        }

        if(!quotaService.canView(nimbusSpace.getId())) {
            throw new UploadQuotaException("Maximum view for this drop exceeded");
        }

        String dropKey = drop.getDropKey();

        quotaService.registerView(nimbusSpace.getId());

        return fileStorageService.openDropFile(dropKey);
    }


    public Resource downloadDropFile(String dropKey, String email, Long nimbusId) throws IOException {
        User user = entityFetcher.getUserByEmail(email);
        Nimbus nimbus = entityFetcher.getNimbusById(nimbusId);
        NimbusSpace nimbusSpace = user.getNimbusSpace();

        if(CheckOwnerUtil.checkNimbusOwnerValidity(nimbus, user)){
            throw new ResourceOwnershipException("You are not allowed to download this drop");
        }

        if(!quotaService.canDownload(nimbusSpace.getId())) {
            throw new UploadQuotaException("Maximum download for this drop exceeded");
        }

        quotaService.registerDownload(nimbusSpace.getId());

        return fileStorageService.dowloadDropFile(dropKey);
    }
}
