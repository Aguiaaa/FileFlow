package com.fileflow;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@Tag(
        name = "Files",
        description = "Upload, manage and download files"
)
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(
        summary = "Upload a file",
        description = "Stores the file on the server and saves its metadata in PostgreSQL."
    )
    @PostMapping(
        value = "/files/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public FileResponse uploadFile(
            @RequestPart("file") MultipartFile file
    ) {
        return fileService.uploadFile(file);
    }

    @Operation(
        summary = "Get all files",
        description = "Returns metadata for every stored file."
    )
    @GetMapping("/files")
    public List<FileResponse> getAllFiles() {
        return fileService.getAllFiles();
    }

    @Operation(
        summary = "Get file metadata"
    )
    @GetMapping("/files/{id}")
    public FileResponse getFile(@PathVariable UUID id) {
        return fileService.getFile(id);
    }

    @Operation(
        summary = "Rename a file"
    )
    @PatchMapping("/files/{id}")
    public FileResponse updateFile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFileRequest request
    ) {
        return fileService.updateFile(id, request.getName());
    }

    @Operation(
        summary = "Delete a file",
        description = "Deletes the file, its metadata and associated share links."
    )
    @DeleteMapping("/files/{id}")
    public void deleteFile(
            @PathVariable UUID id
    ) {

        fileService.deleteFile(id);
    }
    
    @Operation(
        summary = "Download a file"
    )
    @GetMapping("/files/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable UUID id
    )  {

        DownloadFile download =
                fileService.downloadFile(id);

        MediaType mediaType;

        try {
            mediaType =
                    MediaType.parseMediaType(
                            download.getContentType()
                    );
        } catch (Exception exception) {
            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                        download.getFileName() +
                        "\""
                )
                .body(download.getResource());
    }
}