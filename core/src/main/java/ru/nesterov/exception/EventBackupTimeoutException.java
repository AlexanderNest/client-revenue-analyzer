package ru.nesterov.exception;

public class EventBackupTimeoutException extends Exception {
    public EventBackupTimeoutException(long timeoutForNextBackup) {
        super("Следующий бэкап можно будет сделать по прошествии " + timeoutForNextBackup + " минут(ы)");
    }
}
