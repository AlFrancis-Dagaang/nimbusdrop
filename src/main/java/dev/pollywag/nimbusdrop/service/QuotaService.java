package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.entity.NimbusSpace;
import dev.pollywag.nimbusdrop.repository.NimbusSpaceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Service
public class QuotaService {

    private final NimbusSpaceRepository nimbusSpaceRepository;

    public QuotaService(NimbusSpaceRepository nimbusSpaceRepository) {
        this.nimbusSpaceRepository = nimbusSpaceRepository;
    }

    // -------------------- Upload --------------------
    // Check only daily upload limit
    public boolean canUpload(Long nimbusSpaceId) {
        NimbusSpace space = getNimbusSpace(nimbusSpaceId);
        resetUploadIfNewDay(space);
        return space.getUploadsToday() < space.getMaxUploadsPerDay();
    }

    // Check only storage quota
    public boolean canUploadByStorage(Long nimbusSpaceId, Long fileSize) {
        NimbusSpace space = getNimbusSpace(nimbusSpaceId);
        return (space.getUsedStorageBytes() + fileSize) <= space.getMaxStorageBytes();
    }

    public void registerUpload(Long nimbusSpaceId, Long fileSize) {
        NimbusSpace space = getNimbusSpace(nimbusSpaceId);
        resetUploadIfNewDay(space);
        space.setUploadsToday(space.getUploadsToday() + 1);
        space.setUsedStorageBytes(space.getUsedStorageBytes() + fileSize);
        space.setUploadsDate(LocalDateTime.now());
        nimbusSpaceRepository.save(space);
    }

    private void resetUploadIfNewDay(NimbusSpace space) {
        if (space.getUploadsDate() == null || !LocalDate.now().equals(space.getUploadsDate().toLocalDate())) {
            space.setUploadsToday(0L);
            space.setUploadsDate(LocalDateTime.now());
        }
    }

    // -------------------- Download --------------------
    public boolean canDownload(Long nimbusSpaceId) {
        NimbusSpace space = getNimbusSpace(nimbusSpaceId);
        resetDownloadIfNewDay(space);
        return space.getDownloadsToday() < space.getMaxDownloadPerDay();
    }

    public void registerDownload(Long nimbusSpaceId) {
        NimbusSpace space = getNimbusSpace(nimbusSpaceId);
        resetDownloadIfNewDay(space);
        space.setDownloadsToday(space.getDownloadsToday() + 1);
        space.setDownloadsDate(LocalDateTime.now());
        nimbusSpaceRepository.save(space);
    }

    private void resetDownloadIfNewDay(NimbusSpace space) {
        if (space.getDownloadsDate() == null || !LocalDate.now().equals(space.getDownloadsDate().toLocalDate())) {
            space.setDownloadsToday(0);
            space.setDownloadsDate(LocalDateTime.now());
        }
    }

    // -------------------- Views --------------------
    public boolean canView(Long nimbusSpaceId) {
        NimbusSpace space = getNimbusSpace(nimbusSpaceId);
        resetViewIfNewDay(space);
        return space.getViewsToday() < space.getMaxViewsPerDay();
    }

    public void registerView(Long nimbusSpaceId) {
        NimbusSpace space = getNimbusSpace(nimbusSpaceId);
        resetViewIfNewDay(space);
        space.setViewsToday(space.getViewsToday() + 1);
        space.setViewsDate(LocalDateTime.now());
        nimbusSpaceRepository.save(space);
    }

    private void resetViewIfNewDay(NimbusSpace space) {
        if (space.getViewsDate() == null || !LocalDate.now().equals(space.getViewsDate().toLocalDate())) {
            space.setViewsToday(0);
            space.setViewsDate(LocalDateTime.now());
        }
    }

    // -------------------- Helper --------------------
    private NimbusSpace getNimbusSpace(Long nimbusSpaceId) {
        return nimbusSpaceRepository.findById(nimbusSpaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));
    }
}

