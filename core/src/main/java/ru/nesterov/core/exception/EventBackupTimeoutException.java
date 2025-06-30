package ru.nesterov.core.exception;

public class EventBackupTimeoutException extends CoreException {
    public EventBackupTimeoutException(long cooldownMinutes) {
        super(String.valueOf(cooldownMinutes));
    }
}
