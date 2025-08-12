package dev.pollywag.nimbusdrop.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class NimbusSpaceService {

    private final Path STORAGE_ROOT = Paths.get("NimbusSpace");

    public void createUserNimbusSpace(String userDisplayName) {
        try{
            Path userFolder = STORAGE_ROOT.resolve(userDisplayName);
            if(!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
            }
        }catch(IOException e){
            throw new RuntimeException("Failed to create storage folder for " + userDisplayName, e);
        }
    }
}
