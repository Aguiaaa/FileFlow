package com.fileflow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;

import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;
    private final ShareLinkRepository shareLinkRepository;

    public FileService(
            FileRepository fileRepository,
            FileStorageService fileStorageService,
            ShareLinkRepository shareLinkRepository 
    ) {
        this.fileRepository = fileRepository;
        this.fileStorageService = fileStorageService;
        this.shareLinkRepository = shareLinkRepository ; 
    }

    public FileResponse uploadFile(MultipartFile multipartFile)
             {

        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "File must not be empty"
            );
        }

        String path = fileStorageService.store(multipartFile);

        try {
        FileInfo file = new FileInfo();

        file.setName(multipartFile.getOriginalFilename());
        file.setSize(multipartFile.getSize());
        file.setContentType(multipartFile.getContentType());
        file.setStoragePath(path);

        FileInfo saved = fileRepository.save(file);

        return toResponse(saved);

        } catch (RuntimeException exception) {

                try {
                        fileStorageService.delete(path);
                } catch (FileStorageException cleanupException) {
                        exception.addSuppressed(cleanupException);
                }

                throw exception;
                }
    }

    public DownloadFile downloadFile(UUID id) {

        FileInfo file = fileRepository.findById(id)
                .orElseThrow(() ->
                        new FileNotFoundException(id.toString()));

        Resource resource;
        
        resource = fileStorageService.load(file.getStoragePath());
         

        return new DownloadFile(
                resource,
                file.getName(),
                file.getContentType()
        );
    }

    public List<FileResponse> getAllFiles() {

        return fileRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FileResponse getFile(UUID id) {

        FileInfo file = fileRepository.findById(id)
                .orElseThrow(() ->
                        new FileNotFoundException(id.toString()));

        return toResponse(file);
    }

    @Transactional
    public FileResponse updateFile(UUID id, String newName) {

        FileInfo file = fileRepository.findById(id)
                .orElseThrow(() ->
                        new FileNotFoundException(id.toString()));

        file.setName(newName);

        return toResponse(file);
    }
    
    @Transactional
    public void deleteFile(UUID id) {

        FileInfo file = fileRepository.findById(id)
                .orElseThrow(() ->
                        new FileNotFoundException(id.toString()));
        
        shareLinkRepository.deleteByFile_Id(id);
        fileRepository.delete(file);
        fileRepository.flush();

        fileStorageService.delete(file.getStoragePath());

    }
    private FileResponse toResponse(FileInfo file) {
        return new FileResponse(
                file.getId(),
                file.getName(),
                file.getSize(),
                file.getContentType()
        );
    }
}