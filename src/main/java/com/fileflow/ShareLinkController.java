package com.fileflow;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(
        name = "Share Links",
        description = "Create and use temporary file sharing links"
) 
public class ShareLinkController {

    private final ShareLinkService shareLinkService ; 
    private final FileService fileService;


    public ShareLinkController (ShareLinkService shareLinkService, FileService fileService) {
        this.shareLinkService = shareLinkService ; 
        this.fileService = fileService ; 
    }

    @Operation(
        summary = "Create a temporary share link",
        description = "Creates a link valid for 24 hours with a limited number of downloads."
    )
    @PostMapping("/files/{id}/share")
    public ShareLinkResponse createShareLink(@PathVariable UUID id ) {
        return shareLinkService.createShareLink(id) ; 
    }


    @Operation(
        summary = "Download a shared file",
        description = "Downloads a file using a valid share token."
    )
    @GetMapping("/share/{token}")
    public ResponseEntity<Resource> downloadSharedFile(
            @PathVariable String token
        ) {

            DownloadFile download =
                    shareLinkService.downloadSharedFile(token);

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

    @PostMapping(
        value = "/share",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ShareLinkResponse uploadAndShare(@RequestPart("file") MultipartFile file) {
        FileResponse uploaded =
                fileService.uploadFile(file);

        return shareLinkService.createShareLink(uploaded.getId());
    }
}
