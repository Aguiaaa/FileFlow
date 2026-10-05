package com.fileflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "FileFlow API",
                version = "1.0",
                description = "Backend API for file upload, download and temporary sharing."
        )
)
public class FileflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileflowApplication.class, args);
    }
}