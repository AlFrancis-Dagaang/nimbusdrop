package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.entity.Drop;
import dev.pollywag.nimbusdrop.entity.DropSharedLink;
import dev.pollywag.nimbusdrop.entity.NimbusSpace;
import dev.pollywag.nimbusdrop.exception.DropNotFoundException;
import dev.pollywag.nimbusdrop.exception.UploadQuotaException;
import dev.pollywag.nimbusdrop.repository.DropRepository;
import dev.pollywag.nimbusdrop.repository.DropShareLinkRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.time.LocalDateTime;

@Service
public class DropSharedLinkService {

    private final DropShareLinkRepository dropShareLinkRepository;
    private final DropRepository dropRepository;
    private final FileStorageService fileStorageService;
    private final QuotaService quotaService;

    public DropSharedLinkService(DropShareLinkRepository dropShareLinkRepository, DropRepository dropRepository
    , FileStorageService fileStorageService, QuotaService quotaService) {
        this.dropShareLinkRepository = dropShareLinkRepository;
        this.dropRepository = dropRepository;
        this.fileStorageService = fileStorageService;
        this.quotaService = quotaService;
    }

    public Resource dropSharedLink(String token) throws MalformedURLException {
        DropSharedLink dropSharedLink =  dropShareLinkRepository.findByToken(token).orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        LocalDateTime expiresAt = dropSharedLink.getExpiresAt();

        if(expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expired token");
        }

        Drop drop = dropRepository.findById(dropSharedLink.getDropId()).orElseThrow(() -> new DropNotFoundException("Invalid dropId"));

        NimbusSpace nimbusSpace = drop.getNimbus().getNimbusSpace();

        if(!quotaService.canDownload(nimbusSpace.getId())){
            throw new UploadQuotaException("Download quota exceeded");
        }

        String dropKey = drop.getDropKey();

        quotaService.registerDownload(nimbusSpace.getId());

        return fileStorageService.getDropFileLink(dropKey);
    }
}
