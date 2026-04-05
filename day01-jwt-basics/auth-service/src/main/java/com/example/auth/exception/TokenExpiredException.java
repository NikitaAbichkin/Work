package com.example.auth.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() {
        super("Токен истёк");
    }
}

