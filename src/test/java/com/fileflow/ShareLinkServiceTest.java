package com.fileflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.Resource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareLinkServiceTest {

    @Mock
    private ShareLinkRepository shareLinkRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ShareLinkService shareLinkService;

    @Test
    void shouldRejectDownloadWhenLimitIsReached() {

        String token = "test-token";

        ShareLink shareLink = new ShareLink();
        shareLink.setToken(token);
        shareLink.setExpiresAt(
                Instant.now().plusSeconds(3600)
        );
        shareLink.setDownloadCount(5);
        shareLink.setMaxDownloads(5);

        when(shareLinkRepository.findByTokenForUpdate(token))
                .thenReturn(Optional.of(shareLink));

        assertThrows(
                DownloadLimitReachedException.class,
                () -> shareLinkService.downloadSharedFile(token)
        );

        verifyNoInteractions(fileStorageService);
    }

    @Test
    void shouldDownloadFileAndIncrementDownloadCount() {

        String token = "valid-token";

        FileInfo file = new FileInfo();
        file.setName("document.pdf");
        file.setContentType("application/pdf");
        file.setStoragePath("uploads/fake-file");

        ShareLink shareLink = new ShareLink();
        shareLink.setToken(token);
        shareLink.setFile(file);
        shareLink.setExpiresAt(
                Instant.now().plusSeconds(3600)
        );
        shareLink.setDownloadCount(2);
        shareLink.setMaxDownloads(5);

        Resource resource = mock(Resource.class);

        when(shareLinkRepository.findByTokenForUpdate(token))
                .thenReturn(Optional.of(shareLink));

        when(fileStorageService.load("uploads/fake-file"))
                .thenReturn(resource);

        DownloadFile result =
                shareLinkService.downloadSharedFile(token);

        assertEquals(3, shareLink.getDownloadCount());

        assertEquals("document.pdf", result.getFileName());
        assertEquals("application/pdf", result.getContentType());
        assertSame(resource, result.getResource());

        verify(shareLinkRepository)
                .findByTokenForUpdate(token);

        verify(fileStorageService)
                .load("uploads/fake-file");
    }
}