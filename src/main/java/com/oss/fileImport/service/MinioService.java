package com.oss.fileImport.service;

import com.oss.fileImport.exception.MinioException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

@Service
public class MinioService {

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private MinioClient minioClient;

    public MinioService() {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * Asynchronously uploads a file to MinIO.
     *
     * @param file The file to upload.
     * @return A CompletableFuture representing the async upload task.
     */
    public CompletableFuture<Void> uploadFileAsync(File file) {
        return CompletableFuture.runAsync(() -> {
            try (FileInputStream fis = new FileInputStream(file)) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(file.getName())
                        .stream(fis, file.length(), -1)
                        .build());

                // Replaced System.out.println with logger
                logger.info("File uploaded to MinIO: {}", file.getName());
            } catch (IOException | MinioException | InvalidKeyException | NoSuchAlgorithmException |
                     ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                     ServerException | XmlParserException e) {
                // Properly handle specific exceptions and log the error
                logger.error("Failed to upload file to MinIO: {}", file.getName(), e);
                throw new MinioException("Failed to upload file to MinIO: " + file.getName(), e);
            }
        });
    }
}



