package ru.nesterov.exception;

public abstract class CoreException extends RuntimeException {
    public CoreException(String message) {
        super(message);
    }
}
