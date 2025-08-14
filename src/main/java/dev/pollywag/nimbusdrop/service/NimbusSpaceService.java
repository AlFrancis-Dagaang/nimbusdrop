package dev.pollywag.nimbusdrop.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class NimbusSpaceService {

    private final FileStorageService fileStorageService;

    public NimbusSpaceService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void createUserNimbusSpace(String userDisplayName) {
        fileStorageService.createNimbusSpaceDirectory(userDisplayName);
    }
}
