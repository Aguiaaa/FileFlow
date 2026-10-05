package com.fileflow;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;


@Service 
public class ShareLinkService {

    private final ShareLinkRepository shareLinkRepository ; 
    private final FileRepository fileRepository ; 
    private final FileStorageService fileStorageService ; 
    private final String baseUrl;

    public ShareLinkService(ShareLinkRepository shareLinkRepository , FileRepository fileRepository, FileStorageService fileStorageService, @Value("${fileflow.base-url}") String baseUrl) {
        this.shareLinkRepository = shareLinkRepository ; 
        this.fileRepository = fileRepository ; 
        this.fileStorageService = fileStorageService ; 
        this.baseUrl = baseUrl;
    }

    private FileInfo getFile(UUID id) {
        return fileRepository.findById(id)
                .orElseThrow(() ->
                        new FileNotFoundException(id.toString()));
    }

    public ShareLinkResponse createShareLink(UUID fileId) {

        FileInfo file = getFile(fileId);

        ShareLink shareLink = new ShareLink();

        Instant now = Instant.now();

        shareLink.setFile(file);
        shareLink.setCreatedAt(now);
        shareLink.setExpiresAt(now.plus(Duration.ofHours(24)));
        shareLink.setToken(UUID.randomUUID().toString());
        shareLink.setDownloadCount(0);
        shareLink.setMaxDownloads(5);
        ShareLink saved = shareLinkRepository.save(shareLink) ; 
        return toResponse(saved);
    }

    private ShareLink getShareLink(String token) {
        return  shareLinkRepository.findByToken(token).orElseThrow(() ->
                        new ShareLinkNotFoundException(token)); 
    }

    private void validateShareLink(ShareLink shareLink) {

        if (shareLink.getExpiresAt().isBefore(Instant.now())) {
            throw new ShareLinkExpiredException(
                    shareLink.getToken()
            );
        }

        if (shareLink.getDownloadCount()
                >= shareLink.getMaxDownloads()) {
            throw new DownloadLimitReachedException(
                    shareLink.getToken()
            );
        }
    }

    @Transactional
    public DownloadFile downloadSharedFile(String token) {

        ShareLink shareLink =
                getShareLinkForUpdate(token);

        validateShareLink(shareLink);

        shareLink.setDownloadCount(
                shareLink.getDownloadCount() + 1
        );

        FileInfo file = shareLink.getFile();

        Resource resource =
                fileStorageService.load(
                        file.getStoragePath()
                );

        return new DownloadFile(
                resource,
                file.getName(),
                file.getContentType()
        );
    }

    private ShareLink getShareLinkForUpdate(String token) {
        return shareLinkRepository
                .findByTokenForUpdate(token)
                .orElseThrow(() ->
                        new ShareLinkNotFoundException(token));
    }


    private ShareLinkResponse toResponse(ShareLink s) {
        return new ShareLinkResponse(s.getCreatedAt(), s.getExpiresAt(), s.getToken(), baseUrl) ; 
    }

}
