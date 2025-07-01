package ru.nesterov.core.exception;

public abstract class CoreException extends RuntimeException {
    public CoreException(String message) {
        super(message);
    }
}
