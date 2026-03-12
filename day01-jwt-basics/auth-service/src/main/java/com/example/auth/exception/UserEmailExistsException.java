package com.example.auth.exception;

public class UserEmailExistsException extends RuntimeException {
    public UserEmailExistsException() {
        super("Email уже используется");
    }
}

