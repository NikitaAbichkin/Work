package com.example.auth.exception;

public class GoalNotFoundException extends RuntimeException {
    public GoalNotFoundException(Long goalId) {
        super("Цель с ID " + goalId + " не найдена");
    }
}
