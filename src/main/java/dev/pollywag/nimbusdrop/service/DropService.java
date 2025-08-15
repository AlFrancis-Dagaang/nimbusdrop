package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.dto.respondeDTO.DropResponse;
import dev.pollywag.nimbusdrop.entity.Drop;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.entity.NimbusSpace;
import dev.pollywag.nimbusdrop.entity.User;
import dev.pollywag.nimbusdrop.exception.NimbusNotFoundException;
import dev.pollywag.nimbusdrop.exception.UploadQuotaException;
import dev.pollywag.nimbusdrop.repository.DropRepository;
import dev.pollywag.nimbusdrop.repository.NimbusRepository;
import dev.pollywag.nimbusdrop.repository.NimbusSpaceRepository;
import dev.pollywag.nimbusdrop.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class DropService {
    private final DropRepository dropRepository;
    private final UploadQuotaService uploadQuotaService;
    private final NimbusRepository nimbusRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final NimbusSpaceRepository nimbusSpaceRepository;

    public DropService(DropRepository dropRepository, UploadQuotaService uploadQuotaService,
                       NimbusRepository nimbusRepository, FileStorageService fileStorageService
    , UserRepository userRepository, ModelMapper modelMapper, NimbusSpaceRepository nimbusSpaceRepository) {
        this.dropRepository = dropRepository;
        this.uploadQuotaService = uploadQuotaService;
        this.nimbusRepository = nimbusRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.nimbusSpaceRepository = nimbusSpaceRepository;
    }

    public DropResponse uploadDrop(Long nimbusId, MultipartFile file, String email) {
        //Get the user for user displayName
        User user = this.userRepository.findByEmail(email).orElseThrow(() -> new NimbusNotFoundException(email));

        //Check Upload quota
        Nimbus nimbus = nimbusRepository.findNimbusById(nimbusId).orElseThrow(() -> new NimbusNotFoundException("Nimbus not found"));
        if (!uploadQuotaService.canUpload(nimbus.getNimbusSpace().getId(), file.getSize())) {
            throw new UploadQuotaException("Upload quota exceeded");
        }

        String userDisplayName = user.getUserDisplayName();
        String nimbusName = nimbus.getNimbusName();

        //Store Drop File
        fileStorageService.saveDropFile(userDisplayName,nimbusName, file);

        //Create Drop entity
        Drop drop = new Drop();
        drop.setDropName(file.getOriginalFilename());
        drop.setContentType(file.getContentType());
        drop.setSize(file.getSize());
        drop.setNimbus(nimbus);
        drop.setUploadedAt(LocalDateTime.now());

        //Save Nimbus and Drop
        nimbus.getDrops().add(drop);
        dropRepository.save(drop);
        nimbusRepository.save(nimbus);

        //Register upload for quota tracking
        uploadQuotaService.registerUpload(nimbus.getNimbusSpace().getId(), file.getSize());

        return modelMapper.map(drop, DropResponse.class);
    }

    public void deleteDrop(Long dropId) {
        Drop drop = dropRepository.findById(dropId).orElseThrow(() -> new NimbusNotFoundException("Drop not found"));
        Nimbus nimbus = drop.getNimbus();
        NimbusSpace nimbusSpace = drop.getNimbus().getNimbusSpace();
        User user = nimbusSpace.getUser();

        String userDisplayName = user.getUserDisplayName();
        String nimbusName = nimbus.getNimbusName();

        fileStorageService.deleteDrop(userDisplayName, nimbusName, drop.getDropName());

        Long updateStorageBytes = nimbusSpace.getUsedStorageBytes() - drop.getSize();
        nimbusSpace.setUsedStorageBytes(updateStorageBytes);

        nimbusSpaceRepository.save(nimbusSpace);
        dropRepository.delete(drop);
    }









}
