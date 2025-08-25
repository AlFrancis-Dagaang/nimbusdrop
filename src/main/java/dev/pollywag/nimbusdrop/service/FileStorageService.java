package dev.pollywag.nimbusdrop.service;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path STORAGE_ROOT = Paths.get("drop-storage");

    public FileStorageService(){}

    public void deleteNimbusDirectory(String nimbusDirectory) {
        try{
            Path nimbusFolder = STORAGE_ROOT.resolve(nimbusDirectory);
            if(Files.exists(nimbusFolder)) {
                FileUtils.deleteDirectory(nimbusFolder.toFile());
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void emptyNimbusDirectory(String nimbusPath) {
        try{
            File nimbusFolder = STORAGE_ROOT.resolve(nimbusPath).toFile();
            if(nimbusFolder.exists()){
                FileUtils.cleanDirectory(nimbusFolder);
            }
        }catch (IOException e){
            throw new RuntimeException("Failed to clean nimbus directory: " + nimbusPath, e);
        }
    }

    public void saveDropFile(String dropKey, MultipartFile multipartFile) throws IOException {


        Path destination = STORAGE_ROOT.resolve(dropKey);
        Files.createDirectories(destination.getParent());
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public Resource openDropFile(String dropKey ) throws MalformedURLException {

        Path filePath = STORAGE_ROOT.resolve(dropKey);

        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found");
        }

        return new UrlResource(filePath.toUri());
    }

    public void deleteDrop(String dropKey) throws IOException {

        Path filePath = STORAGE_ROOT.resolve(dropKey);

        try{
            if(Files.exists(filePath)){
                Files.delete(filePath);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public Resource downloadDropFile(String dropKey) throws MalformedURLException {
        Path filePath = STORAGE_ROOT.resolve(dropKey).normalize();

        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found");
        }

        return new UrlResource(filePath.toUri());
    }

    public Resource getDropFileLink(String dropKey) throws MalformedURLException {
        Path filePath = STORAGE_ROOT.resolve(dropKey).normalize();

        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found");
        }

        return new UrlResource(filePath.toUri());
    }

    public void deleteUserDropFolder(String dropKey) {
        Path folderPath = STORAGE_ROOT.resolve(dropKey).normalize();

        if (!Files.exists(folderPath)) {
            throw new RuntimeException("Folder not found: " + dropKey);
        }

        try {
            FileUtils.deleteDirectory(folderPath.toFile()); // ✅ deletes folder + all contents
        } catch (IOException e) {
            throw new RuntimeException("Error while deleting folder: " + folderPath, e);
        }
    }



//    public void nimbusRename(String userDisplayName, String newNimbusName, String oldNimbusName) throws IOException {
//        Path oldPath = STORAGE_ROOT.resolve(userDisplayName).resolve(oldNimbusName);
//        Path newPath = STORAGE_ROOT.resolve(userDisplayName).resolve(newNimbusName);
//
//        if (!Files.exists(oldPath)) {
//            throw new IllegalArgumentException("Folder does not exist: " + oldPath);
//        }
//
//        Files.move(oldPath, newPath, StandardCopyOption.ATOMIC_MOVE);
//    }

}
