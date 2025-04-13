package ru.nesterov.exception;

public class UserFriendlyException extends RuntimeException {
    public UserFriendlyException(String message) {
        super(message);
    }
}
