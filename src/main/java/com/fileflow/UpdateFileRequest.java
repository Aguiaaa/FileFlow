package com.fileflow;

import jakarta.validation.constraints.NotBlank;

public class UpdateFileRequest {
    @NotBlank(message = "File name is required")
    private String name;

    public UpdateFileRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}