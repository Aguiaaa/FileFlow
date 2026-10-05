package com.fileflow;

import java.util.UUID;

public class FileResponse {

    private UUID id;
    private String name;
    private long size;
    private String contentType;

    public FileResponse(
            UUID id,
            String name,
            long size,
            String contentType
    ) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.contentType = contentType;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }
}