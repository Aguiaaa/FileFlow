package com.fileflow;

public class ShareLinkNotFoundException extends RuntimeException {

    public ShareLinkNotFoundException(String token) {
        super("Share link not found: " + token);
    }
}