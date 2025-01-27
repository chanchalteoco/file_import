package com.oss.fileImport.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class FileProcessor {

    private final FtpService ftpService;
    private final MinioService minioService;
    private final RedisService redisService;

    private final ExecutorService executor = Executors.newFixedThreadPool(10); // Configurable thread pool

    public FileProcessor(FtpService ftpService, MinioService minioService, RedisService redisService) {
        this.ftpService = ftpService;
        this.minioService = minioService;
        this.redisService = redisService;
    }

    @Scheduled(cron = "${task.cron}")
    public void processFiles() throws IOException {
        // Fetch files from FTP server
        ftpService.fetchFiles();

        // Locate downloaded files
        File localDir = new File(System.getProperty("user.dir") + "/tmp/ftp");
        File[] files = localDir.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No files found to process.");
            return;
        }

        // Process each file asynchronously using CompletableFuture
        Arrays.stream(files).forEach(file ->
                CompletableFuture.runAsync(() -> processFile(file), executor)
                        .exceptionally(ex -> {
                            System.err.println("Error processing file " + file.getName() + ": " + ex.getMessage());
                            return null;
                        })
        );

        // Optionally, shut down the executor service after all tasks are completed
        // executor.shutdown(); (Not recommended here as this runs periodically)
    }

    private void processFile(File file) {
        try {
            // Update metadata to "IN_PROGRESS" asynchronously
            redisService.saveFileMetadataAsync(file.getName(), "IN_PROGRESS")
                    .thenComposeAsync(ignored -> {
                        // Upload file to MinIO asynchronously
                        return minioService.uploadFileAsync(file);
                    }, executor)
                    .thenComposeAsync(ignored -> {
                        // Update metadata to "COMPLETED" asynchronously
                        return redisService.saveFileMetadataAsync(file.getName(), "COMPLETED");
                    }, executor)
                    .exceptionally(ex -> {
                        // Handle any exception during processing and update metadata to "FAILED"
                        redisService.saveFileMetadataAsync(file.getName(), "FAILED");
                        System.err.println("Error during processing of file " + file.getName() + ": " + ex.getMessage());
                        return null;
                    })
                    .join(); // Ensure each file's CompletableFuture chain is complete
        } catch (Exception e) {
            redisService.saveFileMetadataAsync(file.getName(), "FAILED");
            System.err.println("Unexpected error during file processing: " + e.getMessage());
        }
    }
}


