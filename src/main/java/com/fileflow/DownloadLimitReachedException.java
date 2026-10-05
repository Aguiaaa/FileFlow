package com.fileflow;

public class DownloadLimitReachedException extends RuntimeException {

    public DownloadLimitReachedException(String token) {
        super("Download limit reached for share link: " + token);
    }
}