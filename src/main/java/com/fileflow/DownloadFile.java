package com.fileflow;

import org.springframework.core.io.Resource;

public class DownloadFile {

    private final Resource resource;
    private final String fileName;
    private final String contentType;

    public DownloadFile(
            Resource resource,
            String fileName,
            String contentType
    ) {
        this.resource = resource;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public Resource getResource() {
        return resource;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }
} 