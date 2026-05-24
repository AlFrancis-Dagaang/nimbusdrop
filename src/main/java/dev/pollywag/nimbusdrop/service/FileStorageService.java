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

    // Deletes the whole directory for a given Nimbus (including all its files)
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

    // Removes all files inside a Nimbus directory but keeps the folder itself
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

    // Saves an uploaded file to disk under the given drop key path
    public void saveDropFile(String dropKey, MultipartFile multipartFile) throws IOException {


        Path destination = STORAGE_ROOT.resolve(dropKey);
        Files.createDirectories(destination.getParent());
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    // Opens a stored file as a Spring Resource for viewing/streaming
    public Resource openDropFile(String dropKey ) throws MalformedURLException {

        Path filePath = STORAGE_ROOT.resolve(dropKey);

        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found");
        }

        return new UrlResource(filePath.toUri());
    }

    // Deletes a single stored file identified by drop key
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

    // Returns a Resource for downloading a stored file
    public Resource downloadDropFile(String dropKey) throws MalformedURLException {
        Path filePath = STORAGE_ROOT.resolve(dropKey).normalize();

        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found");
        }

        return new UrlResource(filePath.toUri());
    }

    // Returns a Resource that can be used as a link to the stored file
    public Resource getDropFileLink(String dropKey) throws MalformedURLException {
        Path filePath = STORAGE_ROOT.resolve(dropKey).normalize();

        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found");
        }

        return new UrlResource(filePath.toUri());
    }

    // Deletes the root folder for a user's drops and everything inside it
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

}
