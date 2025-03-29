package com.example.openapi.client;

/**
 * HashEx API 异常类
 */
public class HashExApiException extends Exception {
    
    public HashExApiException(String message) {
        super(message);
    }
    
    public HashExApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
