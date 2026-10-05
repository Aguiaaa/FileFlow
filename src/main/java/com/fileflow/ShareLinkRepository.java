package com.fileflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface ShareLinkRepository
        extends JpaRepository<ShareLink, UUID> {

    Optional<ShareLink> findByToken(String token);
    void deleteByFile_Id(UUID fileId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShareLink s WHERE s.token = :token")
    Optional<ShareLink> findByTokenForUpdate(@Param("token") String token);
}