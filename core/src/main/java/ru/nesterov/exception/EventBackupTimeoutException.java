package ru.nesterov.exception;

public class EventBackupTimeoutException extends Exception {
    public EventBackupTimeoutException(long cooldownMinutes) {
        super(String.valueOf(cooldownMinutes));
    }
}
