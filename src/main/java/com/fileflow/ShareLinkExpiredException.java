package com.fileflow;

public class ShareLinkExpiredException extends RuntimeException {

    public ShareLinkExpiredException(String token) {
        super("Share link expired : " + token);
    }
}