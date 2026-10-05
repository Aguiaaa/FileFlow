package com.fileflow;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFileNotFound(
            FileNotFoundException exception
    ) {

        Map<String, String> body = Map.of(
                "error", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }


    @ExceptionHandler(ShareLinkNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleShareLinkNotFound(
        ShareLinkNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", exception.getMessage()
                ));
    }
    @ExceptionHandler(ShareLinkExpiredException.class)
    public ResponseEntity<Map<String, String>> handleShareLinkExpired(
        ShareLinkExpiredException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.GONE)
            .body(Map.of(
                    "error", exception.getMessage()
            ));
    }
    @ExceptionHandler(DownloadLimitReachedException.class)
    public ResponseEntity<Map<String, String>> handleDownloadLimitReached(
        DownloadLimitReachedException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.GONE)
            .body(Map.of(
                    "error", exception.getMessage()
            ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException exception
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", exception.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> fields = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fields.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body = new HashMap<>();

        body.put("error", "Validation failed");
        body.put("fields", fields);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Map<String, String>> handleFileStorage(
                FileStorageException exception
     ) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "File storage error"
                ));
     }
}