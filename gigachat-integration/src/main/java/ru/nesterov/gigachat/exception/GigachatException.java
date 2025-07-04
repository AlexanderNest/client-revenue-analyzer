package ru.nesterov.gigachat.exception;

public class GigachatException extends RuntimeException {
    public GigachatException(String userFriendlyMessage, Throwable cause) {
        super(userFriendlyMessage, cause);
    }
}
