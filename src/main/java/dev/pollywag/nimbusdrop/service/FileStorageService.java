package dev.pollywag.nimbusdrop.service;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path STORAGE_ROOT = Paths.get("NimbusSpace");

    public FileStorageService(){}

    public void createNimbusSpaceDirectory(String userDisplayName) {
        try{
            Path path = STORAGE_ROOT.resolve(userDisplayName);
            if(!Files.exists(path)){
                Files.createDirectory(path);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void createNimbusDirectory(String userDisplayName, String nimbusName) {
        try{
            Path path = STORAGE_ROOT.resolve(userDisplayName).resolve(nimbusName);
            if(!Files.exists(path)){
                Files.createDirectories(path);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void deleteNimbusDirectory(String userDisplayName, String nimbusName) {
        try{
            File nimbusFolder = STORAGE_ROOT.resolve(userDisplayName).resolve(nimbusName).toFile();
            if(nimbusFolder.exists()){
                FileUtils.deleteDirectory(nimbusFolder);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void saveDropFile(String userDisplayName, String nimbusName, MultipartFile file) {

        String originalFileName = file.getOriginalFilename();

        String dropName = UUID.randomUUID() + "_" +
                Paths.get(originalFileName).getFileName().toString();

        Path destination = STORAGE_ROOT.resolve(userDisplayName).resolve(nimbusName).resolve(dropName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

    }











}
