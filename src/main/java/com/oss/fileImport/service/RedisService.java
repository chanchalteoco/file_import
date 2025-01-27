package com.oss.fileImport.service;

import com.oss.fileImport.exception.RedisException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ExecutorService executorService;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        // Creating a fixed thread pool for asynchronous operations
        this.executorService = Executors.newFixedThreadPool(10);  // Configure as per your load
    }

    /**
     * Asynchronously saves file metadata to Redis.
     *
     * @param fileName The name of the file.
     * @param status   The status to save.
     * @return A CompletableFuture representing the async operation.
     */
    public CompletableFuture<Void> saveFileMetadataAsync(String fileName, String status) {
        return CompletableFuture.runAsync(() -> {
            try {
                redisTemplate.opsForHash().put("file:metadata", fileName, status);
                logger.info("Saved metadata for file: {} with status: {}", fileName, status);
            } catch (Exception e) {
                logger.error("Failed to save file metadata in Redis for file: {}", fileName, e);
                throw new RedisException("Failed to save file metadata in Redis for file: " + fileName, e);
            }
        }, executorService);
    }

    /**
     * Asynchronously retrieves file metadata from Redis.
     *
     * @param fileName The name of the file.
     * @return A CompletableFuture with the metadata value.
     */
    public CompletableFuture<String> getFileMetadataAsync(String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object metadata = redisTemplate.opsForHash().get("file:metadata", fileName);
                if (metadata == null) {
                    throw new RedisException("No metadata found for file: " + fileName, new RuntimeException("Metadata not found"));
                }
                logger.info("Retrieved metadata for file: {}", fileName);
                return metadata.toString();
            } catch (Exception e) {
                logger.error("Failed to retrieve file metadata from Redis for file: {}", fileName, e);
                throw new RedisException("Failed to retrieve file metadata from Redis for file: " + fileName, e);
            }
        }, executorService);
    }

    /**
     * Shutdown the executor service when no longer needed.
     */
    public void shutdownExecutorService() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}




