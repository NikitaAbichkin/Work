package com.example.auth.exception;

public class AccountInactiveException extends RuntimeException {
    public AccountInactiveException() {
        super("Учётная запись не активирована");
    }
}

