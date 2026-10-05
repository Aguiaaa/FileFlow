package com.fileflow;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String id) {
        super("File not found: " + id);
    }
}