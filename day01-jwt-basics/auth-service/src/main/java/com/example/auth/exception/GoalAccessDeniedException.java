package com.example.auth.exception;

public class GoalAccessDeniedException extends RuntimeException {
    public GoalAccessDeniedException() {
        super("Нет доступа к этой цели");
    }
}
