package com.fileflow;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageDirectory =  Paths.get("uploads").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException exception) {
            throw new FileStorageException(
                    "Could not initialize storage directory",
                    exception
            );
        }
    }

    public String store(MultipartFile file) {

        try {
            String storedName = UUID.randomUUID().toString();

            Path destination =
                    storageDirectory.resolve(storedName).normalize();

            Files.copy(
                    file.getInputStream(),
                    destination
            );

            return destination.toString();

        } catch (IOException exception) {
            throw new FileStorageException(
                    "Could not store file",
                    exception
            );
        }
    }

    public Resource load(String storagePath) {

        try {
            Path path = Paths.get(storagePath);

            Resource resource =
                    new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new IllegalArgumentException(
                        "Stored file does not exist"
                );
            }

            return resource;
        } catch (MalformedURLException exception) {
            throw new FileStorageException(
                    "Could not load stored file",
                    exception
            );
        }
    }
    
    public void delete(String storagePath) {

        try {
            Path path = Paths.get(storagePath);
            Files.deleteIfExists(path);

        } catch (IOException exception) {
            throw new FileStorageException(
                    "Could not delete stored file",
                    exception
            );
        }
    }

  
}