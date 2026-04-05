package com.example.auth.exception;

public class UserDeleteFailedException extends RuntimeException {
    public UserDeleteFailedException() {
        super("Не удалось удалить пользователя");
    }
}

