package com.oss.fileImport.exception;

public class RedisException extends RuntimeException {
    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }
}
