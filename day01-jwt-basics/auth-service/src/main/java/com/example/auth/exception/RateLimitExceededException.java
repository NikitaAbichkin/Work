package com.example.auth.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException() {
        super("Превышен лимит запросов");
    }
}

