package com.fileflow;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.util.UUID;

@Entity
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private long size;

    private String contentType;
    private String storagePath;

    public FileInfo() {
    }

    public FileInfo(UUID id, String name, long size) {
        this.id = id;
        this.name = name;
        this.size = size;
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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(long size) {
        this.size = size;
    }
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath ; 
    }
    public void setContentType(String contentType) {
        this.contentType = contentType ; 
    }

    public String getStoragePath() {
        return storagePath ;
    }
    public String getContentType() {
    return contentType;
}

}