package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.entity.NimbusSpace;
import dev.pollywag.nimbusdrop.repository.NimbusSpaceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class UploadQuotaService {

    private final NimbusSpaceRepository nimbusSpaceRepository;

    public UploadQuotaService(NimbusSpaceRepository nimbusSpaceRepository) {
        this.nimbusSpaceRepository = nimbusSpaceRepository;
    }

    public boolean canUpload(Long nimbusSpaceId, Long fileSize) {
        NimbusSpace space = nimbusSpaceRepository.findById(nimbusSpaceId).orElseThrow(() -> new IllegalArgumentException("Space not found"));

        // Check daily upload limit
        if (!isWithinDailyLimit(space)) {
            return false;
        }

        // Check storage limit
        return isWithinStorageLimit(space, fileSize);
    }
//    public void resetDailyUploadCounts() {
//        // Reset upload counts for all spaces where date != today
//        nimbusSpaceRepository.resetUploadCountsForDate(LocalDate.now());
//    }
    public void registerUpload(Long nimbusSpaceId, Long fileSize) {
        NimbusSpace space = nimbusSpaceRepository.findById(nimbusSpaceId).orElseThrow(() -> new IllegalArgumentException("Space not found"));
        space.setUploadsToday(space.getUploadsToday() + 1);
        space.setUsedStorageBytes(space.getUsedStorageBytes() + fileSize);
        space.setUploadsDate(LocalDateTime.now());
        nimbusSpaceRepository.save(space);
    }

    private boolean isWithinDailyLimit(NimbusSpace space) {
        if (space.getUploadsDate() == null || !LocalDate.now().equals(space.getUploadsDate().toLocalDate())) {
            // Reset count for new day
            space.setUploadsToday(0L);
            space.setUploadsDate(LocalDateTime.now());
        }
        return space.getUploadsToday() < space.getMaxUploadsPerDay();
    }

    private boolean isWithinStorageLimit(NimbusSpace space, Long fileSize) {
        return (space.getUsedStorageBytes() + fileSize) <= space.getMaxStorageBytes();
    }





}
