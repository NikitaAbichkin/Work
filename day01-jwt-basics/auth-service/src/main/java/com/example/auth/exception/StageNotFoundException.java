package com.example.auth.exception;

public class StageNotFoundException extends RuntimeException {
    public StageNotFoundException(Long stageId) {
        super("Задача с ID " + stageId + " не найдена");
    }
}
