package com.example.auth.exception;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("Учётная запись заблокирована");
    }
}

