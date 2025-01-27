package com.oss.fileImport.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("details", ex.getClass().getSimpleName());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle FTP-related exceptions
    @ExceptionHandler(FtpException.class)
    public ResponseEntity<Object> handleFtpException(FtpException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("details", "FTP Error");
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_GATEWAY);
    }

    // Handle MinIO-related exceptions
    @ExceptionHandler(MinioException.class)
    public ResponseEntity<Object> handleMinioException(MinioException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("details", "MinIO Storage Error");
        return new ResponseEntity<>(errorDetails, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // Handle Redis-related exceptions
    @ExceptionHandler(RedisException.class)
    public ResponseEntity<Object> handleRedisException(RedisException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("details", "Redis Error");
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}

