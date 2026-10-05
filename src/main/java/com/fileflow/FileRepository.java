package com.fileflow;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileInfo, UUID> {
}