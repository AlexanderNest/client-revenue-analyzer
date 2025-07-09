package ru.nesterov.ai.exception;

public class AiException extends RuntimeException {
    public AiException(String message, Throwable cause){
        super(message, cause);
    }
}

